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

package com.tencent.devops.process.yaml.actions.pacActions

import com.tencent.devops.common.api.enums.ScmType
import com.tencent.devops.process.yaml.actions.BaseAction
import com.tencent.devops.process.yaml.actions.GitActionCommon
import com.tencent.devops.process.yaml.actions.data.ActionData
import com.tencent.devops.process.yaml.actions.data.ActionMetaData
import com.tencent.devops.process.yaml.actions.data.EventCommonData
import com.tencent.devops.process.yaml.actions.pacActions.data.PacEnableEvent
import com.tencent.devops.process.yaml.common.Constansts.ciFileDirectoryName
import com.tencent.devops.process.yaml.git.pojo.ApiRequestRetryInfo
import com.tencent.devops.process.yaml.git.pojo.PacGitCred
import com.tencent.devops.process.yaml.git.pojo.tgit.TGitCred
import com.tencent.devops.process.yaml.git.service.PacGitApiService
import com.tencent.devops.process.yaml.pojo.CheckType
import com.tencent.devops.process.yaml.pojo.YamlContent
import com.tencent.devops.process.yaml.pojo.YamlPathListEntry
import com.tencent.devops.process.yaml.v2.enums.StreamObjectKind

class PacEnableAction : BaseAction {
    override val metaData: ActionMetaData = ActionMetaData(StreamObjectKind.ENABLE)

    override lateinit var data: ActionData
    fun event() = data.event as PacEnableEvent

    override lateinit var api: PacGitApiService

    override fun init(): BaseAction? {
        return initCommonData()
    }

    private fun initCommonData(): PacEnableAction {
        val event = event()
        val gitProjectId = getGitProjectIdOrName()
        val defaultBranch = api.getGitProjectInfo(
            cred = this.getGitCred(),
            gitProjectId = gitProjectId,
            retry = ApiRequestRetryInfo(true)
        )!!.defaultBranch!!
        this.data.eventCommon = EventCommonData(
            gitProjectId = gitProjectId,
            userId = event.userId,
            branch = defaultBranch,
            projectName = data.setting.projectName,
            scmType = event.scmType
        )
        this.data.context.defaultBranch = defaultBranch
        return this
    }

    override fun getGitProjectIdOrName(gitProjectId: String?) = gitProjectId ?: data.setting.projectName

    override fun getGitCred(personToken: String?): PacGitCred {
        val event = event()
        return when (event.scmType) {
            ScmType.CODE_GIT, ScmType.CODE_TGIT -> TGitCred(
                userId = event().userId,
                accessToken = personToken,
                useAccessToken = personToken == null
            )
            else -> TODO("对接其他代码库平台时需要补充")
        }
    }

    override fun getYamlPathList(): List<YamlPathListEntry> {
        return GitActionCommon.getYamlPathList(
            action = this,
            gitProjectId = this.getGitProjectIdOrName(),
            ref = this.data.eventCommon.branch
        ).map { (name, blobId) ->
            YamlPathListEntry(name, CheckType.NEED_CHECK, this.data.eventCommon.branch, blobId)
        }
    }

    override fun getYamlContent(fileName: String): YamlContent {
        return YamlContent(
            ref = data.eventCommon.branch,
            content = api.getFileContent(
                cred = this.getGitCred(),
                gitProjectId = getGitProjectIdOrName(),
                fileName = fileName,
                ref = data.eventCommon.branch,
                retry = ApiRequestRetryInfo(true)
            )
        )
    }

    fun getCiDirId(): String? {
        return api.getFileInfo(
            cred = this.getGitCred(),
            gitProjectId = getGitProjectIdOrName(),
            fileName = ciFileDirectoryName,
            ref = data.eventCommon.branch,
            retry = ApiRequestRetryInfo(true)
        )?.blobId
    }

    override fun getChangeSet(): Set<String>? = null

    override fun getDeleteYamlFiles(): Set<String>? = null
}
