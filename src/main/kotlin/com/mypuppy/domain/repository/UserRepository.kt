package com.mypuppy.domain.repository

import com.mypuppy.domain.model.Role
import com.mypuppy.domain.model.User
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository

interface UserRepository : PanacheRepository<User> {

    fun findByEmailAndBusinessId(email: String, businessId: Long): User?

    fun findByBusinessIdAndRole(businessId: Long, role: Role): List<User>
}
