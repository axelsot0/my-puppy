package com.mypuppy.application.service

import com.mypuppy.domain.exception.DuplicateException
import com.mypuppy.domain.exception.InvalidOperationException
import com.mypuppy.domain.exception.NotFoundException
import com.mypuppy.domain.model.EmployeeService
import com.mypuppy.domain.model.Role
import com.mypuppy.domain.repository.EmployeeServiceRepository
import com.mypuppy.domain.repository.ServiceRepository
import com.mypuppy.domain.repository.UserRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.UUID

@ApplicationScoped
class EmployeeServiceService(
    private val employeeServiceRepository: EmployeeServiceRepository,
    private val userRepository: UserRepository,
    private val serviceRepository: ServiceRepository
) {

    fun findByEmployeeId(employeeId: UUID): List<EmployeeService> {
        return employeeServiceRepository.findByEmployeeId(employeeId)
    }

    @Transactional
    fun assign(employeeId: UUID, serviceId: UUID) {
        val employee = userRepository.findById(employeeId)
            ?: throw NotFoundException("Employee", employeeId)

        if (employee.role != Role.EMPLOYEE) {
            throw InvalidOperationException("User is not an employee")
        }

        val service = serviceRepository.findById(serviceId)
            ?: throw NotFoundException("Service", serviceId)

        val existing = employeeServiceRepository.findByEmployeeId(employeeId)
            .any { it.service.id == serviceId }

        if (existing) {
            throw DuplicateException("Employee is already assigned to this service")
        }

        val assignment = EmployeeService().apply {
            this.employee = employee
            this.service = service
        }

        employeeServiceRepository.persist(assignment)
    }

    @Transactional
    fun remove(employeeId: UUID, serviceId: UUID) {
        val assignment = employeeServiceRepository.findByEmployeeId(employeeId)
            .find { it.service.id == serviceId }
            ?: throw NotFoundException("Assignment", "$employeeId-$serviceId")

        employeeServiceRepository.deleteById(assignment.id)
    }
}
