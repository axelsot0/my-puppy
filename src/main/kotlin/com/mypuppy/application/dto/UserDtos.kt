package com.mypuppy.application.dto

import com.mypuppy.domain.model.AuthProvider
import com.mypuppy.domain.model.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.util.UUID

data class RegisterRequest(
    @field:NotBlank
    @field:Email
    val email: String,
    @field:NotBlank
    val firstName: String,
    @field:NotBlank
    val lastName: String,
    val password: String?,
    val authProvider: AuthProvider = AuthProvider.LOCAL,
    val providerId: String? = null
)

data class LoginRequest(
    @field:NotBlank
    @field:Email
    val email: String,
    @field:NotBlank
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserResponse
)

data class LoginChallengeResponse(
    val challengeId: UUID,
    val message: String,
    val expiresInSeconds: Long
)

data class VerifyOtpRequest(
    val challengeId: UUID,
    @field:NotBlank
    @field:Pattern(regexp = "^\\d{6}$")
    val otp: String
)

data class UserResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: Role,
    val authProvider: AuthProvider,
    val active: Boolean
)

data class UpdateUserRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val password: String? = null
)
