package com.mypuppy.api.resource

import com.mypuppy.application.dto.CreateAvailabilityRequest
import com.mypuppy.application.dto.toResponse
import com.mypuppy.application.service.AvailabilityService
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.util.UUID

@Path("/api/availabilities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN", "EMPLOYEE", "SUPER_ADMIN")
class AvailabilityResource(
    private val availabilityService: AvailabilityService
) {

    @GET
    @Path("/employee/{employeeId}")
    fun listByEmployee(@PathParam("employeeId") employeeId: UUID): Response {
        val availabilities = availabilityService.findByEmployeeId(employeeId)
            .map { it.toResponse() }
        return Response.ok(availabilities).build()
    }

    @POST
    fun create(request: CreateAvailabilityRequest): Response {
        val availability = availabilityService.create(
            employeeId = request.employeeId,
            dayOfWeek = request.dayOfWeek,
            startTime = request.startTime,
            endTime = request.endTime
        ).toResponse()

        return Response.status(Response.Status.CREATED).entity(availability).build()
    }

    @DELETE
    @Path("/{id}")
    fun delete(@PathParam("id") id: UUID): Response {
        availabilityService.delete(id)
        return Response.noContent().build()
    }
}
