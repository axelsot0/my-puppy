package com.mypuppy.infrastructure.persistence

import com.mypuppy.domain.model.Availability
import com.mypuppy.domain.repository.AvailabilityRepository
import jakarta.enterprise.context.ApplicationScoped
import java.time.DayOfWeek
import java.util.UUID

@ApplicationScoped
class AvailabilityRepositoryImpl : AvailabilityRepository {

    override fun findByEmployeeId(employeeId: UUID): List<Availability> {
        return list("employee.id", employeeId)
    }

    override fun findByEmployeeIdAndDayOfWeek(employeeId: UUID, dayOfWeek: DayOfWeek): List<Availability> {
        return list("employee.id = ?1 and dayOfWeek = ?2", employeeId, dayOfWeek)
    }
}
