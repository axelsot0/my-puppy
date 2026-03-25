package com.mypuppy.application.dto

import java.time.DayOfWeek
import java.time.LocalTime

data class CreateAvailabilityRequest(
    val employeeId: Long,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
)

data class AvailabilityResponse(
    val id: Long,
    val employeeId: Long,
    val employeeName: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
)
