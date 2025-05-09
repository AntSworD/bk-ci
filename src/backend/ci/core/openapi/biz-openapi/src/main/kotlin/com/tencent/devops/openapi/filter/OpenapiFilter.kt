package com.tencent.devops.openapi.filter

import com.tencent.devops.common.web.RequestFilter
import com.tencent.devops.openapi.filter.manager.ApiFilterManagerChain
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.PreMatching
import jakarta.ws.rs.ext.Provider

@Provider
@PreMatching
@RequestFilter
class OpenapiFilter constructor(
    private val apiFilter: ApiFilterManagerChain
) : ContainerRequestFilter {

    override fun filter(requestContext: ContainerRequestContext) {
        apiFilter.doFilterCheck(requestContext)
    }
}
