package com.mypuppy.infrastructure.tenant

import com.mypuppy.domain.exception.UnauthorizedException
import com.mypuppy.domain.model.Business
import com.mypuppy.domain.repository.BusinessRepository
import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.ext.Provider

@RequestScoped
class TenantContext {

    var businessId: Long? = null

    fun requireBusinessId(): Long {
        return businessId ?: throw UnauthorizedException("Missing tenant identifier")
    }
}

@Provider
class TenantFilter(
    private val tenantContext: TenantContext,
    private val businessRepository: BusinessRepository
) : ContainerRequestFilter {

    companion object {
        const val TENANT_HEADER = "X-Tenant-Id"
    }

    override fun filter(requestContext: ContainerRequestContext) {
        val path = requestContext.uriInfo.path

        if (path.startsWith("platform") || path.startsWith("q/")) {
            return
        }

        val tenantHeader = requestContext.getHeaderString(TENANT_HEADER) ?: return
        val businessId = tenantHeader.toLongOrNull()
            ?: throw UnauthorizedException("Invalid tenant identifier")

        val business: Business = businessRepository.findById(businessId)
            ?: throw UnauthorizedException("Tenant not found")

        if (!business.active) {
            throw UnauthorizedException("Tenant is inactive")
        }

        tenantContext.businessId = businessId
    }
}
