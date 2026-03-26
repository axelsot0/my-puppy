package com.mypuppy.domain.repository

import com.mypuppy.domain.model.Service
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import java.util.UUID

interface ServiceRepository : PanacheRepositoryBase<Service, UUID> {

    fun findByBusinessId(businessId: UUID): List<Service>
}
