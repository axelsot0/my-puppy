package com.mypuppy.application.dto

import com.mypuppy.domain.model.AuthProvider
import com.mypuppy.domain.model.Role
import java.util.UUID

data class RegisterRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String?,
    val authProvider: AuthProvider = AuthProvider.LOCAL,
    val providerId: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserResponse
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
