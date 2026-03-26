package com.mypuppy.application.dto

import com.mypuppy.domain.model.AppointmentStatus
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class BookAppointmentRequest(
    val serviceId: UUID,
    val date: LocalDate,
    val time: LocalTime,
    val notes: String? = null,
    val metadata: String? = null
)

data class AppointmentResponse(
    val id: UUID,
    val clientId: UUID,
    val clientName: String,
    val serviceId: UUID,
    val serviceName: String,
    val employeeId: UUID?,
    val employeeName: String?,
    val date: LocalDate,
    val time: LocalTime,
    val status: AppointmentStatus,
    val notes: String?,
    val metadata: String?
)
