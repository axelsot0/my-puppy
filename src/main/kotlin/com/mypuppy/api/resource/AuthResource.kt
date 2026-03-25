package com.mypuppy.api.resource

import com.mypuppy.application.dto.RegisterRequest
import com.mypuppy.application.dto.toResponse
import com.mypuppy.application.service.UserService
import com.mypuppy.domain.model.Role
import com.mypuppy.infrastructure.tenant.TenantContext
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AuthResource(
    private val userService: UserService,
    private val tenantContext: TenantContext
) {

    @POST
    @Path("/register")
    fun register(request: RegisterRequest): Response {
        val businessId = tenantContext.requireBusinessId()

        val user = userService.register(
            businessId = businessId,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            rawPassword = request.password,
            authProvider = request.authProvider,
            providerId = request.providerId,
            role = Role.CLIENT
        ).toResponse()

        return Response.status(Response.Status.CREATED).entity(user).build()
    }
}
