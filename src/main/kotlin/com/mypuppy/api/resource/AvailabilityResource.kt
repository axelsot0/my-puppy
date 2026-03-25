package com.mypuppy.api.resource

import com.mypuppy.application.dto.CreateAvailabilityRequest
import com.mypuppy.application.dto.toResponse
import com.mypuppy.application.service.AvailabilityService
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/availabilities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AvailabilityResource(
    private val availabilityService: AvailabilityService
) {

    @GET
    @Path("/employee/{employeeId}")
    fun listByEmployee(@PathParam("employeeId") employeeId: Long): Response {
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
    fun delete(@PathParam("id") id: Long): Response {
        availabilityService.delete(id)
        return Response.noContent().build()
    }
}
