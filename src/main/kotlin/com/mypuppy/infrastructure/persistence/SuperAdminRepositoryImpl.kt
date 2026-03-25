package com.mypuppy.infrastructure.persistence

import com.mypuppy.domain.model.SuperAdmin
import com.mypuppy.domain.repository.SuperAdminRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class SuperAdminRepositoryImpl : SuperAdminRepository {

    override fun findByEmail(email: String): SuperAdmin? {
        return find("email", email).firstResult()
    }
}
