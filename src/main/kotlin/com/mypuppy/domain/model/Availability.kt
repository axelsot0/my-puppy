package com.mypuppy.domain.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.DayOfWeek
import java.time.LocalTime

@Entity
@Table(name = "availabilities")
class Availability : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    var employee: User = User()

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var dayOfWeek: DayOfWeek = DayOfWeek.MONDAY

    @field:NotNull
    @Column(nullable = false)
    var startTime: LocalTime = LocalTime.of(9, 0)

    @field:NotNull
    @Column(nullable = false)
    var endTime: LocalTime = LocalTime.of(18, 0)
}
