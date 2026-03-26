package com.mypuppy.infrastructure.tenant

import com.mypuppy.domain.exception.UnauthorizedException
import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.ext.Provider
import org.eclipse.microprofile.jwt.JsonWebToken
import java.util.UUID

@RequestScoped
class TenantContext {

    var businessId: UUID? = null
    var userId: UUID? = null

    fun requireBusinessId(): UUID {
        return businessId ?: throw UnauthorizedException("Missing tenant identifier")
    }

    fun requireUserId(): UUID {
        return userId ?: throw UnauthorizedException("Authentication required")
    }
}

@Provider
class TenantFilter(
    private val tenantContext: TenantContext,
    private val jwt: JsonWebToken
) : ContainerRequestFilter {

    companion object {
        const val TENANT_HEADER = "X-Tenant-Id"
    }

    override fun filter(requestContext: ContainerRequestContext) {
        val path = requestContext.uriInfo.path

        if (path.startsWith("platform") || path.startsWith("q/") || path.contains("auth")) {
            return
        }

        // Try JWT claim first
        val businessClaim = jwt.getClaim<String>("businessId")
        if (businessClaim != null) {
            tenantContext.businessId = UUID.fromString(businessClaim)
            tenantContext.userId = UUID.fromString(jwt.subject)
            return
        }

        // Fallback to header (for public endpoints)
        val tenantHeader = requestContext.getHeaderString(TENANT_HEADER) ?: return
        try {
            tenantContext.businessId = UUID.fromString(tenantHeader)
        } catch (e: IllegalArgumentException) {
            throw UnauthorizedException("Invalid tenant identifier")
        }
    }
}
