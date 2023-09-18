package com.tencent.devops.process.pojo.template

import com.tencent.devops.common.pipeline.Model
import com.tencent.devops.common.pipeline.pojo.setting.PipelineSetting
import com.tencent.devops.process.pojo.transfer.TransferMark
import io.swagger.annotations.ApiModelProperty

data class TemplatePreviewDetail(
    @ApiModelProperty("模板模型")
    val template: Model,
    @ApiModelProperty("模板Yaml")
    val templateYaml: String?,
    @ApiModelProperty("预览流水线设置")
    val setting: PipelineSetting?,
    @ApiModelProperty("高亮位置，可能多个")
    val highlightMarkList: List<TransferMark>?
)
