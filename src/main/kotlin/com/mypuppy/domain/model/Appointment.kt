package com.mypuppy.domain.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "appointments")
class Appointment : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    var client: User = User()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    var service: Service = Service()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    var employee: User? = null

    @field:NotNull
    @Column(nullable = false)
    var date: LocalDate = LocalDate.now()

    @field:NotNull
    @Column(nullable = false)
    var time: LocalTime = LocalTime.now()

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AppointmentStatus = AppointmentStatus.REQUESTED

    @Column(length = 500)
    var notes: String? = null

    @Column(length = 1000)
    var metadata: String? = null
}
