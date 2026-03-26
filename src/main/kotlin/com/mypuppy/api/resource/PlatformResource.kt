package com.mypuppy.api.resource

import com.mypuppy.application.dto.*
import com.mypuppy.application.service.AuthService
import com.mypuppy.application.service.BusinessService
import com.mypuppy.application.service.SuperAdminService
import com.mypuppy.application.service.UserService
import com.mypuppy.domain.model.Role
import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import java.util.UUID

@Path("/platform")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("SUPER_ADMIN")
class PlatformResource(
    private val businessService: BusinessService,
    private val authService: AuthService,
    private val userService: UserService,
    private val superAdminService: SuperAdminService,
    private val jwt: JsonWebToken
) {

    @POST
    @Path("/auth/login")
    @PermitAll
    fun loginSuperAdmin(@Valid request: LoginRequest): Response {
        val challengeId = authService.requestSuperAdminLoginOtp(request.email, request.password)
        val response = LoginChallengeResponse(
            challengeId = challengeId,
            message = "OTP sent to email",
            expiresInSeconds = authService.otpExpirationSeconds()
        )
        return Response.accepted(response).build()
    }

    @POST
    @Path("/auth/verify-otp")
    @PermitAll
    fun verifySuperAdminOtp(@Valid request: VerifyOtpRequest): Response {
        val token = authService.verifySuperAdminLoginOtp(request.challengeId, request.otp)
        return Response.ok(mapOf("token" to token)).build()
    }

    @GET
    @Path("/businesses")
    fun listAll(): Response {
        val businesses = businessService.listAll().map { it.toResponse() }
        return Response.ok(businesses).build()
    }

    @GET
    @Path("/businesses/{id}")
    fun findById(@PathParam("id") id: UUID): Response {
        val business = businessService.findById(id).toResponse()
        return Response.ok(business).build()
    }

    @POST
    @Path("/businesses")
    fun create(request: CreateBusinessRequest): Response {
        val business = businessService.create(
            name = request.name,
            slug = request.slug,
            type = request.type,
            description = request.description,
            address = request.address,
            phone = request.phone
        ).toResponse()

        return Response.status(Response.Status.CREATED).entity(business).build()
    }

    @PUT
    @Path("/businesses/{id}")
    fun update(@PathParam("id") id: UUID, request: UpdateBusinessRequest): Response {
        val business = businessService.update(
            id = id,
            name = request.name,
            type = request.type,
            description = request.description,
            address = request.address,
            phone = request.phone
        ).toResponse()

        return Response.ok(business).build()
    }

    @PUT
    @Path("/businesses/{id}/deactivate")
    fun deactivate(@PathParam("id") id: UUID): Response {
        val business = businessService.deactivate(id).toResponse()
        return Response.ok(business).build()
    }

    @PUT
    @Path("/businesses/{id}/activate")
    fun activate(@PathParam("id") id: UUID): Response {
        val business = businessService.activate(id).toResponse()
        return Response.ok(business).build()
    }

    @POST
    @Path("/businesses/{businessId}/admin")
    fun createAdmin(
        @PathParam("businessId") businessId: UUID,
        @Valid request: RegisterRequest
    ): Response {
        val admin = userService.register(
            businessId = businessId,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            rawPassword = request.password,
            role = Role.ADMIN
        ).toResponse()

        return Response.status(Response.Status.CREATED).entity(admin).build()
    }

    // --- Super Admin Management ---

    @GET
    @Path("/admins")
    fun listSuperAdmins(): Response {
        val admins = superAdminService.listAll().map {
            SuperAdminResponse(id = it.id, email = it.email, name = it.name)
        }
        return Response.ok(admins).build()
    }

    @POST
    @Path("/admins")
    fun createSuperAdmin(@Valid request: CreateSuperAdminRequest): Response {
        val admin = superAdminService.create(
            email = request.email,
            password = request.password,
            name = request.name
        )
        val response = SuperAdminResponse(id = admin.id, email = admin.email, name = admin.name)
        return Response.status(Response.Status.CREATED).entity(response).build()
    }

    @DELETE
    @Path("/admins/{id}")
    fun deleteSuperAdmin(@PathParam("id") id: UUID): Response {
        val currentAdminId = UUID.fromString(jwt.subject)
        superAdminService.delete(id, currentAdminId)
        return Response.noContent().build()
    }
}
