package com.mypuppy.infrastructure.security

import com.mypuppy.application.service.AuthService
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter
import jakarta.ws.rs.ext.Provider
import org.eclipse.microprofile.jwt.JsonWebToken

@Provider
class TokenRenewalFilter(
    private val jwt: JsonWebToken,
    private val authService: AuthService
) : ContainerResponseFilter {

    override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext) {
        if (jwt.subject == null || jwt.groups.isNullOrEmpty()) {
            return
        }

        val newToken = authService.refreshToken(jwt)
        responseContext.headers.putSingle("X-Refreshed-Token", newToken)
    }
}
