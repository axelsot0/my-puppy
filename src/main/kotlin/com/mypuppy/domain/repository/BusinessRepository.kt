package com.mypuppy.domain.repository

import com.mypuppy.domain.model.Business
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import java.util.UUID

interface BusinessRepository : PanacheRepositoryBase<Business, UUID> {

    fun findByActive(active: Boolean): List<Business>

    fun findBySlug(slug: String): Business?
}
