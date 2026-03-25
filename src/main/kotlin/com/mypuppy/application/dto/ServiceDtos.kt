package com.mypuppy.application.dto

import java.math.BigDecimal

data class CreateServiceRequest(
    val name: String,
    val description: String? = null,
    val price: BigDecimal,
    val durationMinutes: Int
)

data class ServiceResponse(
    val id: Long,
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
