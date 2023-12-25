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

package com.tencent.devops.process.pojo

import com.tencent.devops.common.api.pojo.PipelineAsCodeSettings
import com.tencent.devops.common.pipeline.enums.VersionStatus
import com.tencent.devops.common.pipeline.pojo.setting.PipelineRunLockType
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("流水线预览页完整信息")
data class PipelineDetail(
    @ApiModelProperty("流水线Id")
    val pipelineId: String,
    @ApiModelProperty("流水线名称")
    val pipelineName: String,
    @ApiModelProperty("是否收藏")
    val hasCollect: Boolean,
    @ApiModelProperty("是否可以手动触发")
    val canManualStartup: Boolean,
    @ApiModelProperty("是否可以调试")
    val canDebug: Boolean,
    @ApiModelProperty("是否可以发布")
    val canRelease: Boolean,
    @ApiModelProperty("是否从模板实例化")
    val instanceFromTemplate: Boolean,
    @ApiModelProperty("草稿或最新的发布版本")
    val version: Int,
    @ApiModelProperty("草稿或最新的发布版本名称")
    val versionName: String?,
    @ApiModelProperty("基准版本的状态", required = false)
    val baseVersionStatus: VersionStatus,
    @ApiModelProperty("基准版本的分支名")
    val baseVersionBranch: String?,
    @ApiModelProperty("草稿或最新的发布版本")
    val releaseVersion: Int?,
    @ApiModelProperty("草稿或最新的发布版本名称")
    val releaseVersionName: String?,
    @ApiModelProperty("是否有编辑权限")
    val hasPermission: Boolean,
    @ApiModelProperty("流水线描述")
    val pipelineDesc: String,
    @ApiModelProperty("创建者")
    val creator: String,
    @ApiModelProperty("创建时间")
    val createTime: Long = 0,
    @ApiModelProperty("更新时间")
    val updateTime: Long = 0,
    @ApiModelProperty("流水线组名称列表", required = false)
    var viewNames: List<String>? = null,
    @ApiModelProperty("Lock 类型", required = false)
    val runLockType: PipelineRunLockType? = null,
    @ApiModelProperty("仅存在草稿", required = false)
    var onlyDraft: Boolean? = false,
    @ApiModelProperty("PAC配置", required = false)
    val pipelineAsCodeSettings: PipelineAsCodeSettings?
)
