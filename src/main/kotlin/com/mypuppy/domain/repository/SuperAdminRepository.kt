package com.mypuppy.domain.repository

import com.mypuppy.domain.model.SuperAdmin
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import java.util.UUID

interface SuperAdminRepository : PanacheRepositoryBase<SuperAdmin, UUID> {

    fun findByEmail(email: String): SuperAdmin?
}
