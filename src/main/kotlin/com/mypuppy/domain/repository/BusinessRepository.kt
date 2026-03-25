package com.mypuppy.domain.repository

import com.mypuppy.domain.model.Business
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository

interface BusinessRepository : PanacheRepository<Business> {

    fun findByActive(active: Boolean): List<Business>
}
