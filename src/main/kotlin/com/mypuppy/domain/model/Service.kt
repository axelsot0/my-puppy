package com.mypuppy.domain.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

@Entity
@Table(name = "services")
class Service : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    var business: Business = Business()

    @field:NotBlank
    @Column(nullable = false)
    var name: String = ""

    @Column(length = 500)
    var description: String? = null

    @field:NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO

    @Column(nullable = false)
    var durationMinutes: Int = 30

    @Column(nullable = false)
    var active: Boolean = true
}
