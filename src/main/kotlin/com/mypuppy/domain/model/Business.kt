package com.mypuppy.domain.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "businesses")
class Business : BaseEntity() {

    @field:NotBlank
    @Column(nullable = false)
    var name: String = ""

    @field:NotBlank
    @Column(nullable = false)
    var slug: String = ""

    @field:NotBlank
    @Column(nullable = false)
    var type: String = ""

    @Column(length = 500)
    var description: String? = null

    @Column
    var address: String? = null

    @Column
    var phone: String? = null

    @Column(nullable = false)
    var active: Boolean = true

    @OneToMany(mappedBy = "business")
    var services: MutableList<Service> = mutableListOf()
}
