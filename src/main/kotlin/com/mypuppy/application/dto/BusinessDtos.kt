package com.mypuppy.application.dto

data class CreateBusinessRequest(
    val name: String,
    val slug: String,
    val type: String,
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null
)

data class BusinessResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val type: String,
    val description: String?,
    val address: String?,
    val phone: String?,
    val active: Boolean
)

data class UpdateBusinessRequest(
    val name: String? = null,
    val type: String? = null,
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null
)
