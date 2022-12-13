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
package com.tencent.devops.common.pipeline.pojo.time

import io.swagger.annotations.ApiModel

@ApiModel("构建详情记录-时间戳类型（勿随意删除）")
enum class BuildTimestampType(val action: String) {
    STAGE_CHECK_IN_WAITING("stage准入等待"),
    STAGE_CHECK_OUT_WAITING("stage准出等待"),
    JOB_MUTEX_WAITING("job互斥并发等待"),
    JOB_THIRD_PARTY_WAITING("job第三方构建机资源等待"),
    JOB_CONTAINER_STARTUP("job构建机启动（包含了第三方构建机资源等待）"),
    JOB_CONTAINER_SHUTDOWN("job构建机关闭"),
    TASK_ATOM_LOADING("task可执行文件加载等待"),
    TASK_REVIEW_WAITING("插件审核等待（包括人工审核，质量用心审核）");
}
