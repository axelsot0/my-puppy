package com.mypuppy.domain.model

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "super_admins")
class SuperAdmin : BaseEntity() {

    @field:NotBlank
    @field:Email
    @Column(nullable = false, unique = true)
    var email: String = ""

    @field:NotBlank
    @Column(nullable = false)
    var password: String = ""

    @field:NotBlank
    @Column(nullable = false)
    var name: String = ""
}
