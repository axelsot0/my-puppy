package com.mypuppy.domain.repository

import com.mypuppy.domain.model.Role
import com.mypuppy.domain.model.User
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import java.util.UUID

interface UserRepository : PanacheRepositoryBase<User, UUID> {

    fun findByEmailAndBusinessId(email: String, businessId: UUID): User?

    fun findByBusinessIdAndRole(businessId: UUID, role: Role): List<User>
}
