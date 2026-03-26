package com.mypuppy.application.service

import com.mypuppy.domain.exception.InvalidOperationException
import com.mypuppy.domain.exception.NotFoundException
import com.mypuppy.domain.model.Availability
import com.mypuppy.domain.model.Role
import com.mypuppy.domain.repository.AvailabilityRepository
import com.mypuppy.domain.repository.UserRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

@ApplicationScoped
class AvailabilityService(
    private val availabilityRepository: AvailabilityRepository,
    private val userRepository: UserRepository
) {

    fun findByEmployeeId(employeeId: UUID): List<Availability> {
        return availabilityRepository.findByEmployeeId(employeeId)
    }

    @Transactional
    fun create(employeeId: UUID, dayOfWeek: DayOfWeek, startTime: LocalTime, endTime: LocalTime): Availability {
        val employee = userRepository.findById(employeeId)
            ?: throw NotFoundException("Employee", employeeId)

        if (employee.role != Role.EMPLOYEE && employee.role != Role.ADMIN) {
            throw InvalidOperationException("User is not an employee")
        }

        if (!endTime.isAfter(startTime)) {
            throw InvalidOperationException("End time must be after start time")
        }

        val availability = Availability().apply {
            this.employee = employee
            this.dayOfWeek = dayOfWeek
            this.startTime = startTime
            this.endTime = endTime
        }

        availabilityRepository.persist(availability)
        return availability
    }

    @Transactional
    fun update(id: UUID, dayOfWeek: DayOfWeek?, startTime: LocalTime?, endTime: LocalTime?): Availability {
        val availability = availabilityRepository.findById(id)
            ?: throw NotFoundException("Availability", id)

        dayOfWeek?.let { availability.dayOfWeek = it }
        startTime?.let { availability.startTime = it }
        endTime?.let { availability.endTime = it }

        if (!availability.endTime.isAfter(availability.startTime)) {
            throw InvalidOperationException("End time must be after start time")
        }

        return availability
    }

    @Transactional
    fun delete(id: UUID): Boolean {
        return availabilityRepository.deleteById(id)
    }
}
