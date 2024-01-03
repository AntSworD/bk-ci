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
 *
 */

package com.tencent.devops.process.engine.dao

import com.tencent.devops.model.process.tables.TPipelineYamlVersion
import com.tencent.devops.model.process.tables.records.TPipelineYamlVersionRecord
import com.tencent.devops.process.pojo.pipeline.PipelineYamlVersion
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * 流水线与代码库yml文件关联表
 */
@Repository
class PipelineYamlVersionDao {

    fun save(
        dslContext: DSLContext,
        projectId: String,
        repoHashId: String,
        filePath: String,
        blobId: String,
        commitId: String,
        ref: String?,
        pipelineId: String,
        version: Int,
        versionName: String,
        userId: String
    ) {
        val now = LocalDateTime.now()
        with(TPipelineYamlVersion.T_PIPELINE_YAML_VERSION) {
            dslContext.insertInto(
                this,
                PROJECT_ID,
                REPO_HASH_ID,
                FILE_PATH,
                BLOB_ID,
                COMMIT_ID,
                REF,
                PIPELINE_ID,
                VERSION,
                VERSION_NAME,
                CREATOR,
                MODIFIER,
                CREATE_TIME,
                UPDATE_TIME
            ).values(
                projectId,
                repoHashId,
                filePath,
                blobId,
                commitId,
                ref,
                pipelineId,
                version,
                versionName,
                userId,
                userId,
                now,
                now
            ).execute()
        }
    }

    fun get(
        dslContext: DSLContext,
        projectId: String,
        repoHashId: String,
        filePath: String,
        blobId: String
    ): PipelineYamlVersion? {
        with(TPipelineYamlVersion.T_PIPELINE_YAML_VERSION) {
            val record = dslContext.selectFrom(this)
                .where(PROJECT_ID.eq(projectId))
                .and(REPO_HASH_ID.eq(repoHashId))
                .and(FILE_PATH.eq(filePath))
                .and(BLOB_ID.eq(blobId))
                .fetchOne()
            return record?.let { convert(it) }
        }
    }

    fun getByPipelineId(
        dslContext: DSLContext,
        projectId: String,
        pipelineId: String,
        version: Int
    ): PipelineYamlVersion? {
        with(TPipelineYamlVersion.T_PIPELINE_YAML_VERSION) {
            val record = dslContext.selectFrom(this)
                .where(PROJECT_ID.eq(projectId))
                .and(PIPELINE_ID.eq(pipelineId))
                .and(VERSION.eq(version))
                .fetchAny()
            return record?.let { convert(it) }
        }
    }

    fun deleteAll(
        dslContext: DSLContext,
        projectId: String,
        repoHashId: String,
        filePath: String
    ) {
        with(TPipelineYamlVersion.T_PIPELINE_YAML_VERSION) {
            dslContext.deleteFrom(this)
                .where(PROJECT_ID.eq(projectId))
                .and(REPO_HASH_ID.eq(repoHashId))
                .and(FILE_PATH.eq(filePath))
                .execute()
        }
    }

    fun deleteByBlobId(
        dslContext: DSLContext,
        projectId: String,
        repoHashId: String,
        filePath: String,
        blobId: String
    ) {
        with(TPipelineYamlVersion.T_PIPELINE_YAML_VERSION) {
            dslContext.deleteFrom(this)
                .where(PROJECT_ID.eq(projectId))
                .and(REPO_HASH_ID.eq(repoHashId))
                .and(FILE_PATH.eq(filePath))
                .and(BLOB_ID.eq(blobId))
        }
    }

    fun convert(record: TPipelineYamlVersionRecord): PipelineYamlVersion {
        return with(record) {
            PipelineYamlVersion(
                projectId = projectId,
                repoHashId = repoHashId,
                filePath = filePath,
                blobId = blobId,
                commitId = commitId,
                ref = ref,
                pipelineId = pipelineId,
                version = version,
                versionName = versionName
            )
        }
    }
}
