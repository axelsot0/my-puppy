package com.mypuppy.infrastructure.exception

import com.mypuppy.domain.exception.*
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class DomainExceptionMapper : ExceptionMapper<DomainException> {

    override fun toResponse(exception: DomainException): Response {
        val status = when (exception) {
            is NotFoundException -> Response.Status.NOT_FOUND
            is DuplicateException -> Response.Status.CONFLICT
            is SlotNotAvailableException -> Response.Status.CONFLICT
            is SlotOverlapException -> Response.Status.CONFLICT
            is UnauthorizedException -> Response.Status.UNAUTHORIZED
            is InvalidOperationException -> Response.Status.BAD_REQUEST
            else -> Response.Status.INTERNAL_SERVER_ERROR
        }

        return Response.status(status)
            .entity(ErrorResponse(status.statusCode, exception.message ?: "Unknown error"))
            .build()
    }
}

data class ErrorResponse(
    val status: Int,
    val message: String
)
