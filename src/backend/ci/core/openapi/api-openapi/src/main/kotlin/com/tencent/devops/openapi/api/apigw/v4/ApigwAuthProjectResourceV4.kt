package com.tencent.devops.openapi.api.apigw.v4

import com.tencent.devops.auth.pojo.vo.ProjectPermissionInfoVO
import com.tencent.devops.common.api.auth.AUTH_HEADER_DEVOPS_APP_CODE
import com.tencent.devops.common.api.auth.AUTH_HEADER_DEVOPS_APP_CODE_DEFAULT_VALUE
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.auth.api.AuthResourceType
import com.tencent.devops.common.auth.api.pojo.BkAuthGroup
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.HeaderParam
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Api(tags = ["OPENAPI_AUTH_V4"], description = "OPENAPI-权限相关")
@Path("/{apigwType:apigw-user|apigw-app|apigw}/v4/auth/project/{projectId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Suppress("ALL")
interface ApigwAuthProjectResourceV4 {
    @GET
    @Path("/get_project_permission_info")
    @ApiOperation(
        "获取项目权限信息",
        tags = ["v4_app_get_project_permission_info"]
    )
    fun getProjectPermissionInfo(
        @ApiParam(value = "appCode", required = true, defaultValue = AUTH_HEADER_DEVOPS_APP_CODE_DEFAULT_VALUE)
        @HeaderParam(AUTH_HEADER_DEVOPS_APP_CODE)
        appCode: String?,
        @ApiParam(value = "apigw Type", required = true)
        @PathParam("apigwType")
        apigwType: String?,
        @PathParam("projectId")
        @ApiParam("项目ID", required = true)
        projectId: String
    ): Result<ProjectPermissionInfoVO>

    @GET
    @Path("/getResourceGroupUsers")
    @ApiOperation(
        "获取项目权限分组成员",
        tags = ["v4_app_get_project_permission_members"]
    )
    fun getResourceGroupUsers(
        @ApiParam(value = "appCode", required = true, defaultValue = AUTH_HEADER_DEVOPS_APP_CODE_DEFAULT_VALUE)
        @HeaderParam(AUTH_HEADER_DEVOPS_APP_CODE)
        appCode: String?,
        @ApiParam(value = "apigw Type", required = true)
        @PathParam("apigwType")
        apigwType: String?,
        @PathParam("projectId")
        @ApiParam("项目Code", required = true)
        projectId: String,
        @QueryParam("resourceType")
        @ApiParam("资源类型", required = false)
        resourceType: AuthResourceType,
        @QueryParam("resourceCode")
        @ApiParam("资源code", required = false)
        resourceCode: String,
        @QueryParam("group")
        @ApiParam("资源用户组类型", required = false)
        group: BkAuthGroup? = null
    ): Result<List<String>>
}
