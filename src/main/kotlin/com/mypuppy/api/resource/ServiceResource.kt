package com.mypuppy.api.resource

import com.mypuppy.application.dto.CreateServiceRequest
import com.mypuppy.application.dto.UpdateServiceRequest
import com.mypuppy.application.dto.toResponse
import com.mypuppy.application.service.ServiceService
import com.mypuppy.infrastructure.tenant.TenantContext
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/services")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ServiceResource(
    private val serviceService: ServiceService,
    private val tenantContext: TenantContext
) {

    @GET
    fun list(): Response {
        val businessId = tenantContext.requireBusinessId()
        val services = serviceService.findByBusinessId(businessId).map { it.toResponse() }
        return Response.ok(services).build()
    }

    @GET
    @Path("/{id}")
    fun findById(@PathParam("id") id: Long): Response {
        val service = serviceService.findById(id).toResponse()
        return Response.ok(service).build()
    }

    @POST
    fun create(request: CreateServiceRequest): Response {
        val businessId = tenantContext.requireBusinessId()

        val service = serviceService.create(
            businessId = businessId,
            name = request.name,
            description = request.description,
            price = request.price,
            durationMinutes = request.durationMinutes
        ).toResponse()

        return Response.status(Response.Status.CREATED).entity(service).build()
    }

    @PUT
    @Path("/{id}")
    fun update(@PathParam("id") id: Long, request: UpdateServiceRequest): Response {
        val service = serviceService.update(
            id = id,
            name = request.name,
            description = request.description,
            price = request.price,
            durationMinutes = request.durationMinutes
        ).toResponse()

        return Response.ok(service).build()
    }

    @DELETE
    @Path("/{id}")
    fun deactivate(@PathParam("id") id: Long): Response {
        serviceService.deactivate(id)
        return Response.noContent().build()
    }
}
