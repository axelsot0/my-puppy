package com.mypuppy.domain.repository

import com.mypuppy.domain.model.Service
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository

interface ServiceRepository : PanacheRepository<Service> {

    fun findByBusinessId(businessId: Long): List<Service>
}
