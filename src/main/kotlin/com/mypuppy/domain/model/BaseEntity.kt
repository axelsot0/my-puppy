package com.mypuppy.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@MappedSuperclass
abstract class BaseEntity {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    var id: UUID = UUID.randomUUID()

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @PrePersist
    fun onPrePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
