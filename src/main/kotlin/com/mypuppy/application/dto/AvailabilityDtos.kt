package com.mypuppy.application.dto

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

data class CreateAvailabilityRequest(
    val employeeId: UUID,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
)

data class AvailabilityResponse(
    val id: UUID,
    val employeeId: UUID,
    val employeeName: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
)
