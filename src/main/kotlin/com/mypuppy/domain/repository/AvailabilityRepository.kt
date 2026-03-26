package com.mypuppy.domain.repository

import com.mypuppy.domain.model.Availability
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import java.time.DayOfWeek
import java.util.UUID

interface AvailabilityRepository : PanacheRepositoryBase<Availability, UUID> {

    fun findByEmployeeId(employeeId: UUID): List<Availability>

    fun findByEmployeeIdAndDayOfWeek(employeeId: UUID, dayOfWeek: DayOfWeek): List<Availability>
}
