package com.mypuppy.application.dto

import com.mypuppy.domain.model.AppointmentStatus
import java.time.LocalDate
import java.time.LocalTime

data class BookAppointmentRequest(
    val serviceId: Long,
    val date: LocalDate,
    val time: LocalTime,
    val notes: String? = null,
    val metadata: String? = null
)

data class AppointmentResponse(
    val id: Long,
    val clientId: Long,
    val clientName: String,
    val serviceId: Long,
    val serviceName: String,
    val employeeId: Long?,
    val employeeName: String?,
    val date: LocalDate,
    val time: LocalTime,
    val status: AppointmentStatus,
    val notes: String?,
    val metadata: String?
)
