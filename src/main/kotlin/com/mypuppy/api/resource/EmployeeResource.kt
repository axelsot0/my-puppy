package com.mypuppy.api.resource

import com.mypuppy.application.dto.RegisterRequest
import com.mypuppy.application.dto.toResponse
import com.mypuppy.application.service.EmployeeServiceService
import com.mypuppy.application.service.UserService
import com.mypuppy.domain.model.Role
import com.mypuppy.infrastructure.tenant.TenantContext
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class EmployeeResource(
    private val userService: UserService,
    private val employeeServiceService: EmployeeServiceService,
    private val tenantContext: TenantContext
) {

    @GET
    fun list(): Response {
        val businessId = tenantContext.requireBusinessId()
        val employees = userService.listByBusinessAndRole(businessId, Role.EMPLOYEE)
            .map { it.toResponse() }
        return Response.ok(employees).build()
    }

    @POST
    fun create(request: RegisterRequest): Response {
        val businessId = tenantContext.requireBusinessId()

        val employee = userService.register(
            businessId = businessId,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            rawPassword = request.password,
            authProvider = request.authProvider,
            providerId = request.providerId,
            role = Role.EMPLOYEE
        ).toResponse()

        return Response.status(Response.Status.CREATED).entity(employee).build()
    }

    @POST
    @Path("/{employeeId}/services/{serviceId}")
    fun assignService(
        @PathParam("employeeId") employeeId: Long,
        @PathParam("serviceId") serviceId: Long
    ): Response {
        employeeServiceService.assign(employeeId, serviceId)
        return Response.noContent().build()
    }

    @DELETE
    @Path("/{employeeId}/services/{serviceId}")
    fun removeService(
        @PathParam("employeeId") employeeId: Long,
        @PathParam("serviceId") serviceId: Long
    ): Response {
        employeeServiceService.remove(employeeId, serviceId)
        return Response.noContent().build()
    }

    @DELETE
    @Path("/{id}")
    fun deactivate(@PathParam("id") id: Long): Response {
        userService.deactivate(id)
        return Response.noContent().build()
    }
}
