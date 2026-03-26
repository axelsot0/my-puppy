package com.mypuppy.api.resource

import com.mypuppy.application.dto.BookAppointmentRequest
import com.mypuppy.application.dto.toResponse
import com.mypuppy.application.service.AppointmentService
import com.mypuppy.infrastructure.tenant.TenantContext
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate
import java.util.UUID

@Path("/api/appointments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AppointmentResource(
    private val appointmentService: AppointmentService,
    private val tenantContext: TenantContext
) {

    @GET
    @Path("/mine")
    @RolesAllowed("CLIENT")
    fun listMyAppointments(): Response {
        val clientId = tenantContext.requireUserId()
        val appointments = appointmentService.findByClientId(clientId)
            .map { it.toResponse() }
        return Response.ok(appointments).build()
    }

    @GET
    @Path("/employee/{employeeId}")
    @RolesAllowed("ADMIN", "EMPLOYEE")
    fun listByEmployee(
        @PathParam("employeeId") employeeId: UUID,
        @QueryParam("date") date: LocalDate?
    ): Response {
        val effectiveDate = date ?: LocalDate.now()
        val appointments = appointmentService.findByEmployeeIdAndDate(employeeId, effectiveDate)
            .map { it.toResponse() }
        return Response.ok(appointments).build()
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("CLIENT", "ADMIN", "EMPLOYEE")
    fun findById(@PathParam("id") id: UUID): Response {
        val appointment = appointmentService.findById(id).toResponse()
        return Response.ok(appointment).build()
    }

    @POST
    @RolesAllowed("CLIENT")
    fun book(request: BookAppointmentRequest): Response {
        val clientId = tenantContext.requireUserId()

        val appointment = appointmentService.book(
            clientId = clientId,
            serviceId = request.serviceId,
            date = request.date,
            time = request.time,
            notes = request.notes,
            metadata = request.metadata
        ).toResponse()

        return Response.status(Response.Status.CREATED).entity(appointment).build()
    }

    @PUT
    @Path("/{id}/assign/{employeeId}")
    @RolesAllowed("ADMIN")
    fun assignEmployee(
        @PathParam("id") id: UUID,
        @PathParam("employeeId") employeeId: UUID
    ): Response {
        val appointment = appointmentService.assignEmployee(id, employeeId).toResponse()
        return Response.ok(appointment).build()
    }

    @PUT
    @Path("/{id}/done")
    @RolesAllowed("ADMIN", "EMPLOYEE")
    fun markDone(@PathParam("id") id: UUID): Response {
        val appointment = appointmentService.markDone(id).toResponse()
        return Response.ok(appointment).build()
    }

    @PUT
    @Path("/{id}/reject")
    @RolesAllowed("ADMIN")
    fun reject(@PathParam("id") id: UUID): Response {
        val appointment = appointmentService.reject(id).toResponse()
        return Response.ok(appointment).build()
    }

    @PUT
    @Path("/{id}/cancel")
    @RolesAllowed("CLIENT", "ADMIN")
    fun cancel(@PathParam("id") id: UUID): Response {
        val appointment = appointmentService.cancel(id).toResponse()
        return Response.ok(appointment).build()
    }
}
