package com.mypuppy.domain.model

import jakarta.persistence.*

@Entity
@Table(
    name = "employee_services",
    uniqueConstraints = [UniqueConstraint(columnNames = ["employee_id", "service_id"])]
)
class EmployeeService : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    var employee: User = User()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    var service: Service = Service()
}
