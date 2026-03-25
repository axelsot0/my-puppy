package com.mypuppy.infrastructure.persistence

import com.mypuppy.domain.model.EmployeeService
import com.mypuppy.domain.repository.EmployeeServiceRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class EmployeeServiceRepositoryImpl : EmployeeServiceRepository {

    override fun findByEmployeeId(employeeId: UUID): List<EmployeeService> {
        return list("employee.id", employeeId)
    }

    override fun findByServiceId(serviceId: UUID): List<EmployeeService> {
        return list("service.id", serviceId)
    }
}
