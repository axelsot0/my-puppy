package com.mypuppy.infrastructure.persistence

import com.mypuppy.domain.model.Appointment
import com.mypuppy.domain.model.AppointmentStatus
import com.mypuppy.domain.repository.AppointmentRepository
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate

@ApplicationScoped
class AppointmentRepositoryImpl : AppointmentRepository {

    override fun findByClientId(clientId: Long): List<Appointment> {
        return list("client.id", clientId)
    }

    override fun findByEmployeeIdAndDate(employeeId: Long, date: LocalDate): List<Appointment> {
        return list("employee.id = ?1 and date = ?2", employeeId, date)
    }

    override fun findByServiceId(serviceId: Long): List<Appointment> {
        return list("service.id", serviceId)
    }

    override fun findByStatus(status: AppointmentStatus): List<Appointment> {
        return list("status", status)
    }
}
