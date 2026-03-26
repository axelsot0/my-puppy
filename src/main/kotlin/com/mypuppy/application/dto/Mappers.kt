package com.mypuppy.application.dto

import com.mypuppy.domain.model.*

fun User.toResponse() = UserResponse(
    id = id!!,
    email = email,
    firstName = firstName,
    lastName = lastName,
    role = role,
    authProvider = authProvider,
    active = active
)

fun Business.toResponse() = BusinessResponse(
    id = id!!,
    name = name,
    slug = slug,
    type = type,
    description = description,
    address = address,
    phone = phone,
    active = active
)

fun Service.toResponse() = ServiceResponse(
    id = id!!,
    name = name,
    description = description,
    price = price,
    durationMinutes = durationMinutes,
    active = active
)

fun Availability.toResponse() = AvailabilityResponse(
    id = id!!,
    employeeId = employee.id!!,
    employeeName = "${employee.firstName} ${employee.lastName}",
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime
)

fun Appointment.toResponse() = AppointmentResponse(
    id = id!!,
    clientId = client.id!!,
    clientName = "${client.firstName} ${client.lastName}",
    serviceId = service.id!!,
    serviceName = service.name,
    employeeId = employee?.id,
    employeeName = employee?.let { "${it.firstName} ${it.lastName}" },
    date = date,
    time = time,
    status = status,
    notes = notes,
    metadata = metadata
)
