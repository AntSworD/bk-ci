/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.devops.process.engine.service

import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.redis.RedisOperation
import com.tencent.devops.process.constant.ProcessMessageCode
import com.tencent.devops.process.dao.PipelineSettingVersionDao
import com.tencent.devops.process.engine.control.lock.PipelineVersionLock
import com.tencent.devops.process.engine.dao.PipelineBuildDao
import com.tencent.devops.process.engine.dao.PipelineResourceVersionDao
import com.tencent.devops.process.engine.pojo.PipelineInfo
import com.tencent.devops.process.engine.pojo.PipelineVersionWithInfo
import com.tencent.devops.process.pojo.setting.PipelineVersionSimple
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Service

@Service
@Suppress("LongParameterList", "ReturnCount")
class PipelineRepositoryVersionService(
    private val dslContext: DSLContext,
    private val pipelineResourceVersionDao: PipelineResourceVersionDao,
    private val pipelineSettingVersionDao: PipelineSettingVersionDao,
    private val pipelineBuildDao: PipelineBuildDao,
    private val redisOperation: RedisOperation
) {

    fun addVerRef(projectId: String, pipelineId: String, resourceVersion: Int) {
        PipelineVersionLock(redisOperation, pipelineId, resourceVersion).use { versionLock ->
            versionLock.lock()
            // 查询流水线版本记录
            val pipelineVersionInfo = pipelineResourceVersionDao.getPipelineVersionSimple(
                dslContext = dslContext,
                projectId = projectId,
                pipelineId = pipelineId,
                version = resourceVersion
            )
            val referFlag = pipelineVersionInfo?.referFlag ?: true
            val referCount = pipelineVersionInfo?.referCount?.let { self -> self + 1 }
            // 兼容老数据缺少关联构建记录的情况，全量统计关联数据数量
                ?: pipelineBuildDao.countTotalBuildNumByVersion(
                    dslContext = dslContext,
                    projectId = projectId,
                    pipelineId = pipelineId,
                    version = resourceVersion
                )

            // 更新流水线版本关联构建记录信息
            pipelineResourceVersionDao.updatePipelineVersionReferInfo(
                dslContext = dslContext,
                projectId = projectId,
                pipelineId = pipelineId,
                version = resourceVersion,
                referCount = referCount,
                referFlag = referFlag
            )
        }
    }

    fun deletePipelineVer(projectId: String, pipelineId: String, version: Int) {
        // 判断该流水线版本是否还有关联的构建记录，没有记录才能删除
        val pipelineVersionLock = PipelineVersionLock(redisOperation, pipelineId, version)
        try {
            pipelineVersionLock.lock()
            val count = pipelineBuildDao.countTotalBuildNumByVersion(
                dslContext = dslContext,
                projectId = projectId,
                pipelineId = pipelineId,
                version = version
            )
            if (count > 0) {
                throw ErrorCodeException(
                    errorCode = ProcessMessageCode.ERROR_PIPELINE_CAN_NOT_DELETE_WHEN_HAVE_BUILD_RECORD
                )
            }
            dslContext.transaction { t ->
                val transactionContext = DSL.using(t)
                pipelineResourceVersionDao.deleteByVer(transactionContext, projectId, pipelineId, version)
                pipelineSettingVersionDao.deleteByVer(transactionContext, projectId, pipelineId, version)
            }
        } finally {
            pipelineVersionLock.unlock()
        }
    }

    fun getPipelineVersionWithInfo(
        pipelineInfo: PipelineInfo?,
        projectId: String,
        pipelineId: String,
        version: Int
    ): PipelineVersionWithInfo? {
        if (pipelineInfo == null) {
            return null
        }
        val resource = pipelineResourceVersionDao.getVersionResource(
            dslContext = dslContext,
            projectId = projectId,
            pipelineId = pipelineId,
            version = version,
            includeDraft = true
        ) ?: return null
        return PipelineVersionWithInfo(
            createTime = pipelineInfo.createTime,
            creator = pipelineInfo.creator,
            canElementSkip = pipelineInfo.canElementSkip,
            canManualStartup = pipelineInfo.canManualStartup,
            channelCode = pipelineInfo.channelCode,
            id = pipelineInfo.id,
            lastModifyUser = pipelineInfo.lastModifyUser,
            pipelineDesc = pipelineInfo.pipelineDesc,
            pipelineId = pipelineInfo.pipelineId,
            pipelineName = pipelineInfo.pipelineName,
            projectId = pipelineInfo.projectId,
            taskCount = pipelineInfo.taskCount,
            templateId = pipelineInfo.templateId,
            version = resource.version,
            versionName = resource.versionName ?: "init",
            pipelineVersion = resource.pipelineVersion,
            triggerVersion = resource.triggerVersion,
            settingVersion = resource.settingVersion,
            status = resource.status,
            debugBuildId = resource.debugBuildId,
            baseVersion = resource.baseVersion
        )
    }

    fun getPipelineVersionSimple(
        projectId: String,
        pipelineId: String,
        version: Int
    ): PipelineVersionSimple? {
        return pipelineResourceVersionDao.getPipelineVersionSimple(
            dslContext = dslContext,
            projectId = projectId,
            pipelineId = pipelineId,
            version = version
        )
    }

    fun listPipelineVersion(
        pipelineInfo: PipelineInfo?,
        projectId: String,
        pipelineId: String,
        offset: Int,
        limit: Int,
        excludeVersion: Int?,
        versionName: String?,
        creator: String?,
        description: String?
    ): Pair<Int, MutableList<PipelineVersionWithInfo>> {
        if (pipelineInfo == null) {
            return Pair(0, mutableListOf())
        }

        val count = pipelineResourceVersionDao.count(
            dslContext = dslContext,
            projectId = projectId,
            pipelineId = pipelineId,
            creator = creator,
            description = description
        )
        val result = pipelineResourceVersionDao.listPipelineVersion(
            dslContext = dslContext,
            projectId = projectId,
            pipelineId = pipelineId,
            creator = creator,
            description = description,
            versionName = versionName,
            excludeVersion = excludeVersion,
            offset = offset,
            limit = limit
        )
        val list = mutableListOf<PipelineVersionWithInfo>()

        result.forEach {
            list.add(
                PipelineVersionWithInfo(
                    createTime = pipelineInfo.createTime,
                    creator = pipelineInfo.creator,
                    canElementSkip = pipelineInfo.canElementSkip,
                    canManualStartup = pipelineInfo.canManualStartup,
                    channelCode = pipelineInfo.channelCode,
                    id = pipelineInfo.id,
                    lastModifyUser = pipelineInfo.lastModifyUser,
                    pipelineDesc = pipelineInfo.pipelineDesc,
                    pipelineId = pipelineInfo.pipelineId,
                    pipelineName = pipelineInfo.pipelineName,
                    projectId = pipelineInfo.projectId,
                    taskCount = pipelineInfo.taskCount,
                    templateId = pipelineInfo.templateId,
                    version = it.version,
                    versionName = it.versionName,
                    pipelineVersion = it.pipelineVersion,
                    triggerVersion = it.triggerVersion,
                    settingVersion = it.settingVersion,
                    status = it.status,
                    debugBuildId = it.debugBuildId,
                    baseVersion = it.baseVersion
                )
            )
        }
        return count to list
    }

    fun getVersionCreatorInPage(
        pipelineInfo: PipelineInfo?,
        projectId: String,
        pipelineId: String,
        offset: Int,
        limit: Int
    ): Pair<Int, List<String>> {
        if (pipelineInfo == null) {
            return Pair(0, emptyList())
        }

        val count = pipelineResourceVersionDao.countVersionCreator(
            dslContext = dslContext,
            projectId = projectId,
            pipelineId = pipelineId
        )
        val result = pipelineResourceVersionDao.getVersionCreatorInPage(
            dslContext = dslContext,
            projectId = projectId,
            pipelineId = pipelineId,
            offset = offset,
            limit = limit
        )
        return count to result
    }
}
