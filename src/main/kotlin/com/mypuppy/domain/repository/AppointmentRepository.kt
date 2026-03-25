package com.mypuppy.domain.repository

import com.mypuppy.domain.model.Appointment
import com.mypuppy.domain.model.AppointmentStatus
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import java.time.LocalDate

interface AppointmentRepository : PanacheRepository<Appointment> {

    fun findByClientId(clientId: Long): List<Appointment>

    fun findByEmployeeIdAndDate(employeeId: Long, date: LocalDate): List<Appointment>

    fun findByServiceId(serviceId: Long): List<Appointment>

    fun findByStatus(status: AppointmentStatus): List<Appointment>
}
