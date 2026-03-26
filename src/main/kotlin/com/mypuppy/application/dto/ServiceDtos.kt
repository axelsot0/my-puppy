package com.mypuppy.application.dto

import java.math.BigDecimal
import java.util.UUID

data class CreateServiceRequest(
    val name: String,
    val description: String? = null,
    val price: BigDecimal,
    val durationMinutes: Int
)

data class ServiceResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val durationMinutes: Int,
    val active: Boolean
)

data class UpdateServiceRequest(
    val name: String? = null,
    val description: String? = null,
    val price: BigDecimal? = null,
    val durationMinutes: Int? = null
)
