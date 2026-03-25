package com.mypuppy.infrastructure.persistence

import com.mypuppy.domain.model.Role
import com.mypuppy.domain.model.User
import com.mypuppy.domain.repository.UserRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class UserRepositoryImpl : UserRepository {

    override fun findByEmailAndBusinessId(email: String, businessId: Long): User? {
        return find("email = ?1 and business.id = ?2", email, businessId).firstResult()
    }

    override fun findByBusinessIdAndRole(businessId: Long, role: Role): List<User> {
        return list("business.id = ?1 and role = ?2", businessId, role)
    }
}
