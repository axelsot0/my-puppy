package com.mypuppy.infrastructure.persistence

import com.mypuppy.domain.model.EmployeeService
import com.mypuppy.domain.repository.EmployeeServiceRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class EmployeeServiceRepositoryImpl : EmployeeServiceRepository {

    override fun findByEmployeeId(employeeId: Long): List<EmployeeService> {
        return list("employee.id", employeeId)
    }

    override fun findByServiceId(serviceId: Long): List<EmployeeService> {
        return list("service.id", serviceId)
    }
}
