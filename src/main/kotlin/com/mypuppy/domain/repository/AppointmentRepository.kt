package com.mypuppy.domain.repository

import com.mypuppy.domain.model.Appointment
import com.mypuppy.domain.model.AppointmentStatus
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import java.time.LocalDate
import java.util.UUID

interface AppointmentRepository : PanacheRepositoryBase<Appointment, UUID> {

    fun findByClientId(clientId: UUID): List<Appointment>

    fun findByEmployeeIdAndDate(employeeId: UUID, date: LocalDate): List<Appointment>

    fun findByServiceId(serviceId: UUID): List<Appointment>

    fun findByStatus(status: AppointmentStatus): List<Appointment>
}
