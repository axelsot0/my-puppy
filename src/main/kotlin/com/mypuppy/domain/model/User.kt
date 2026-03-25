package com.mypuppy.domain.model

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Entity
@Table(
    name = "users",
    uniqueConstraints = [UniqueConstraint(columnNames = ["email", "business_id"])]
)
class User : BaseEntity() {

    @field:NotBlank
    @field:Email
    @Column(nullable = false)
    var email: String = ""

    @field:NotBlank
    @Column(nullable = false)
    var firstName: String = ""

    @field:NotBlank
    @Column(nullable = false)
    var lastName: String = ""

    @Column
    var password: String? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var authProvider: AuthProvider = AuthProvider.LOCAL

    @Column
    var providerId: String? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.CLIENT

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    var business: Business = Business()

    @Column(nullable = false)
    var active: Boolean = true
}
