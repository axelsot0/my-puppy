package com.mypuppy.api.resource

import com.mypuppy.application.dto.CreateBusinessRequest
import com.mypuppy.application.dto.UpdateBusinessRequest
import com.mypuppy.application.dto.toResponse
import com.mypuppy.application.service.BusinessService
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/platform/businesses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class PlatformResource(
    private val businessService: BusinessService
) {

    @GET
    fun listAll(): Response {
        val businesses = businessService.listAll().map { it.toResponse() }
        return Response.ok(businesses).build()
    }

    @GET
    @Path("/{id}")
    fun findById(@PathParam("id") id: Long): Response {
        val business = businessService.findById(id).toResponse()
        return Response.ok(business).build()
    }

    @POST
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
    @Path("/{id}")
    fun update(@PathParam("id") id: Long, request: UpdateBusinessRequest): Response {
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
    @Path("/{id}/deactivate")
    fun deactivate(@PathParam("id") id: Long): Response {
        val business = businessService.deactivate(id).toResponse()
        return Response.ok(business).build()
    }

    @PUT
    @Path("/{id}/activate")
    fun activate(@PathParam("id") id: Long): Response {
        val business = businessService.activate(id).toResponse()
        return Response.ok(business).build()
    }
}
