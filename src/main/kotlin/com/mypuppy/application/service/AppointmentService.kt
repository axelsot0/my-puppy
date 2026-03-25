package com.mypuppy.application.service

import com.mypuppy.domain.exception.InvalidOperationException
import com.mypuppy.domain.exception.NotFoundException
import com.mypuppy.domain.exception.SlotNotAvailableException
import com.mypuppy.domain.exception.SlotOverlapException
import com.mypuppy.domain.model.Appointment
import com.mypuppy.domain.model.AppointmentStatus
import com.mypuppy.domain.model.User
import com.mypuppy.domain.repository.AppointmentRepository
import com.mypuppy.domain.repository.AvailabilityRepository
import com.mypuppy.domain.repository.EmployeeServiceRepository
import com.mypuppy.domain.repository.ServiceRepository
import com.mypuppy.domain.repository.UserRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.LocalDate
import java.time.LocalTime

@ApplicationScoped
class AppointmentService(
    private val appointmentRepository: AppointmentRepository,
    private val serviceRepository: ServiceRepository,
    private val userRepository: UserRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val employeeServiceRepository: EmployeeServiceRepository
) {

    fun findById(id: Long): Appointment {
        return appointmentRepository.findById(id)
            ?: throw NotFoundException("Appointment", id)
    }

    fun findByClientId(clientId: Long): List<Appointment> {
        return appointmentRepository.findByClientId(clientId)
    }

    fun findByEmployeeIdAndDate(employeeId: Long, date: LocalDate): List<Appointment> {
        return appointmentRepository.findByEmployeeIdAndDate(employeeId, date)
    }

    @Transactional
    fun book(clientId: Long, serviceId: Long, date: LocalDate, time: LocalTime, notes: String?, metadata: String?): Appointment {
        val client = userRepository.findById(clientId)
            ?: throw NotFoundException("Client", clientId)

        val service = serviceRepository.findById(serviceId)
            ?: throw NotFoundException("Service", serviceId)

        if (!service.active) {
            throw InvalidOperationException("Service is not available")
        }

        val employee = findAvailableEmployee(serviceId, date, time, service.durationMinutes)

        val appointment = Appointment().apply {
            this.client = client
            this.service = service
            this.employee = employee
            this.date = date
            this.time = time
            this.status = if (employee != null) AppointmentStatus.ACCEPTED else AppointmentStatus.REQUESTED
            this.notes = notes
            this.metadata = metadata
        }

        appointmentRepository.persist(appointment)
        return appointment
    }

    @Transactional
    fun assignEmployee(appointmentId: Long, employeeId: Long): Appointment {
        val appointment = findById(appointmentId)

        if (appointment.status != AppointmentStatus.REQUESTED) {
            throw InvalidOperationException("Only requested appointments can be assigned")
        }

        val employee = userRepository.findById(employeeId)
            ?: throw NotFoundException("Employee", employeeId)

        appointment.employee = employee
        appointment.status = AppointmentStatus.ACCEPTED
        return appointment
    }

    @Transactional
    fun markDone(id: Long): Appointment {
        val appointment = findById(id)

        if (appointment.status != AppointmentStatus.ACCEPTED) {
            throw InvalidOperationException("Only accepted appointments can be marked as done")
        }

        appointment.status = AppointmentStatus.DONE
        return appointment
    }

    @Transactional
    fun reject(id: Long): Appointment {
        val appointment = findById(id)

        if (appointment.status != AppointmentStatus.REQUESTED) {
            throw InvalidOperationException("Only requested appointments can be rejected")
        }

        appointment.status = AppointmentStatus.REJECTED
        return appointment
    }

    @Transactional
    fun cancel(id: Long): Appointment {
        val appointment = findById(id)

        if (appointment.status == AppointmentStatus.DONE || appointment.status == AppointmentStatus.REJECTED) {
            throw InvalidOperationException("Cannot cancel this appointment")
        }

        appointment.status = AppointmentStatus.CANCELLED
        return appointment
    }

    private fun findAvailableEmployee(serviceId: Long, date: LocalDate, time: LocalTime, durationMinutes: Int): User? {
        val endTime = time.plusMinutes(durationMinutes.toLong())
        val dayOfWeek = date.dayOfWeek

        val employeesForService = employeeServiceRepository.findByServiceId(serviceId)
            .map { it.employee }

        for (employee in employeesForService) {
            val slots = availabilityRepository.findByEmployeeIdAndDayOfWeek(employee.id!!, dayOfWeek)

            val isAvailable = slots.any { slot ->
                !time.isBefore(slot.startTime) && !endTime.isAfter(slot.endTime)
            }

            if (!isAvailable) continue

            val existingAppointments = appointmentRepository.findByEmployeeIdAndDate(employee.id!!, date)
                .filter { it.status == AppointmentStatus.ACCEPTED }

            val hasOverlap = existingAppointments.any { appt ->
                val apptEnd = appt.time.plusMinutes(appt.service.durationMinutes.toLong())
                time.isBefore(apptEnd) && endTime.isAfter(appt.time)
            }

            if (!hasOverlap) return employee
        }

        return null
    }
}
