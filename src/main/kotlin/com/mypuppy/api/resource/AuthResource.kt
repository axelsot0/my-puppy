package com.mypuppy.api.resource

import com.mypuppy.application.dto.*
import com.mypuppy.application.service.AuthService
import com.mypuppy.application.service.UserService
import com.mypuppy.domain.model.Role
import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.util.UUID

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
class AuthResource(
    private val userService: UserService,
    private val authService: AuthService
) {

    @POST
    @Path("/register")
    fun register(@HeaderParam("X-Tenant-Id") tenantId: UUID, request: RegisterRequest): Response {
        val user = userService.register(
            businessId = tenantId,
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

    @POST
    @Path("/login")
    fun login(@HeaderParam("X-Tenant-Id") tenantId: UUID, request: LoginRequest): Response {
        val (token, user) = authService.loginUser(request.email, request.password, tenantId)
        val response = AuthResponse(token = token, user = user.toResponse())
        return Response.ok(response).build()
    }
}
