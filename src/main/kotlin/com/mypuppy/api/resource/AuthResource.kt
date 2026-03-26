package com.mypuppy.api.resource

import com.mypuppy.application.dto.*
import com.mypuppy.application.service.AuthService
import com.mypuppy.application.service.UserService
import com.mypuppy.domain.model.Role
import jakarta.annotation.security.PermitAll
import jakarta.validation.Valid
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
    fun register(@HeaderParam("X-Tenant-Id") tenantId: UUID?, @Valid request: RegisterRequest): Response {
        val tenant = tenantId ?: throw com.mypuppy.domain.exception.UnauthorizedException("X-Tenant-Id header is required")
        val user = userService.register(
            businessId = tenant,
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
    fun login(@HeaderParam("X-Tenant-Id") tenantId: UUID?, @Valid request: LoginRequest): Response {
        val tenant = tenantId ?: throw com.mypuppy.domain.exception.UnauthorizedException("X-Tenant-Id header is required")
        val challengeId = authService.requestUserLoginOtp(request.email, request.password, tenant)
        val response = LoginChallengeResponse(
            challengeId = challengeId,
            message = "OTP sent to email",
            expiresInSeconds = authService.otpExpirationSeconds()
        )
        return Response.accepted(response).build()
    }

    @POST
    @Path("/verify-otp")
    fun verifyOtp(@HeaderParam("X-Tenant-Id") tenantId: UUID?, @Valid request: VerifyOtpRequest): Response {
        val tenant = tenantId ?: throw com.mypuppy.domain.exception.UnauthorizedException("X-Tenant-Id header is required")
        val (token, user) = authService.verifyUserLoginOtp(request.challengeId, request.otp, tenant)
        val response = AuthResponse(token = token, user = user.toResponse())
        return Response.ok(response).build()
    }
}
