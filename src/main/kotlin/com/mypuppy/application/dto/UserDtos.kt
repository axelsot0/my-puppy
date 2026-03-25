package com.mypuppy.application.dto

import com.mypuppy.domain.model.AuthProvider
import com.mypuppy.domain.model.Role

data class RegisterRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String?,
    val authProvider: AuthProvider = AuthProvider.LOCAL,
    val providerId: String? = null
)

data class UserResponse(
    val id: Long,
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
