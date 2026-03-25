package com.mypuppy.domain.repository

import com.mypuppy.domain.model.Availability
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import java.time.DayOfWeek

interface AvailabilityRepository : PanacheRepository<Availability> {

    fun findByEmployeeId(employeeId: Long): List<Availability>

    fun findByEmployeeIdAndDayOfWeek(employeeId: Long, dayOfWeek: DayOfWeek): List<Availability>
}
