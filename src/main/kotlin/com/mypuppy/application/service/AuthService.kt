package com.mypuppy.application.service

import com.mypuppy.domain.exception.UnauthorizedException
import com.mypuppy.domain.model.User
import com.mypuppy.domain.repository.SuperAdminRepository
import com.mypuppy.domain.repository.UserRepository
import io.quarkus.elytron.security.common.BcryptUtil
import io.smallrye.jwt.build.Jwt
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.jwt.JsonWebToken
import java.time.Duration
import java.util.UUID

@ApplicationScoped
class AuthService(
    private val userRepository: UserRepository,
    private val superAdminRepository: SuperAdminRepository
) {

    fun loginUser(email: String, password: String, businessId: UUID): Pair<String, User> {
        val user = userRepository.findByEmailAndBusinessId(email, businessId)
            ?: throw UnauthorizedException("Invalid credentials")

        if (!user.active) {
            throw UnauthorizedException("Account is disabled")
        }

        val hash = user.password ?: throw UnauthorizedException("Use Google login for this account")

        if (!BcryptUtil.matches(password, hash)) {
            throw UnauthorizedException("Invalid credentials")
        }

        val token = generateUserToken(user)
        return Pair(token, user)
    }

    fun loginSuperAdmin(email: String, password: String): String {
        val admin = superAdminRepository.findByEmail(email)
            ?: throw UnauthorizedException("Invalid credentials")

        if (!BcryptUtil.matches(password, admin.password)) {
            throw UnauthorizedException("Invalid credentials")
        }

        return Jwt.issuer("my-puppy")
            .subject(admin.id.toString())
            .upn(admin.email)
            .groups(setOf("SUPER_ADMIN"))
            .expiresIn(TOKEN_DURATION)
            .sign()
    }

    private fun generateUserToken(user: User): String {
        return Jwt.issuer("my-puppy")
            .subject(user.id.toString())
            .upn(user.email)
            .groups(setOf(user.role.name))
            .claim("businessId", user.business.id.toString())
            .expiresIn(TOKEN_DURATION)
            .sign()
    }

    fun refreshToken(jwt: JsonWebToken): String {
        val groups = jwt.groups
        val builder = Jwt.issuer("my-puppy")
            .subject(jwt.subject)
            .upn(jwt.name)
            .groups(groups)
            .expiresIn(TOKEN_DURATION)

        val businessId = jwt.getClaim<String>("businessId")
        if (businessId != null) {
            builder.claim("businessId", businessId)
        }

        return builder.sign()
    }

    companion object {
        val TOKEN_DURATION: Duration = Duration.ofMinutes(5)
    }
}
