package com.mypuppy.domain.repository

import com.mypuppy.domain.model.EmployeeService
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import java.util.UUID

interface EmployeeServiceRepository : PanacheRepositoryBase<EmployeeService, UUID> {

    fun findByEmployeeId(employeeId: UUID): List<EmployeeService>

    fun findByServiceId(serviceId: UUID): List<EmployeeService>
}
