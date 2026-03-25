package com.mypuppy.domain.repository

import com.mypuppy.domain.model.EmployeeService
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository

interface EmployeeServiceRepository : PanacheRepository<EmployeeService> {

    fun findByEmployeeId(employeeId: Long): List<EmployeeService>

    fun findByServiceId(serviceId: Long): List<EmployeeService>
}
