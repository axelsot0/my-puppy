package com.mypuppy.api.resource

import com.mypuppy.application.dto.BookAppointmentRequest
import com.mypuppy.application.dto.toResponse
import com.mypuppy.application.service.AppointmentService
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate

@Path("/api/appointments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AppointmentResource(
    private val appointmentService: AppointmentService
) {

    @GET
    @Path("/client/{clientId}")
    fun listByClient(@PathParam("clientId") clientId: Long): Response {
        val appointments = appointmentService.findByClientId(clientId)
            .map { it.toResponse() }
        return Response.ok(appointments).build()
    }

    @GET
    @Path("/employee/{employeeId}")
    fun listByEmployee(
        @PathParam("employeeId") employeeId: Long,
        @QueryParam("date") date: LocalDate?
    ): Response {
        val effectiveDate = date ?: LocalDate.now()
        val appointments = appointmentService.findByEmployeeIdAndDate(employeeId, effectiveDate)
            .map { it.toResponse() }
        return Response.ok(appointments).build()
    }

    @GET
    @Path("/{id}")
    fun findById(@PathParam("id") id: Long): Response {
        val appointment = appointmentService.findById(id).toResponse()
        return Response.ok(appointment).build()
    }

    @POST
    fun book(request: BookAppointmentRequest, @HeaderParam("X-User-Id") clientId: Long): Response {
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
    fun assignEmployee(
        @PathParam("id") id: Long,
        @PathParam("employeeId") employeeId: Long
    ): Response {
        val appointment = appointmentService.assignEmployee(id, employeeId).toResponse()
        return Response.ok(appointment).build()
    }

    @PUT
    @Path("/{id}/done")
    fun markDone(@PathParam("id") id: Long): Response {
        val appointment = appointmentService.markDone(id).toResponse()
        return Response.ok(appointment).build()
    }

    @PUT
    @Path("/{id}/reject")
    fun reject(@PathParam("id") id: Long): Response {
        val appointment = appointmentService.reject(id).toResponse()
        return Response.ok(appointment).build()
    }

    @PUT
    @Path("/{id}/cancel")
    fun cancel(@PathParam("id") id: Long): Response {
        val appointment = appointmentService.cancel(id).toResponse()
        return Response.ok(appointment).build()
    }
}
