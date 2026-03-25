package com.mypuppy.domain.repository

import com.mypuppy.domain.model.SuperAdmin
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository

interface SuperAdminRepository : PanacheRepository<SuperAdmin> {

    fun findByEmail(email: String): SuperAdmin?
}
