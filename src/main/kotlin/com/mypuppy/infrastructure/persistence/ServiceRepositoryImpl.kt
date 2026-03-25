package com.mypuppy.infrastructure.persistence

import com.mypuppy.domain.model.Service
import com.mypuppy.domain.repository.ServiceRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ServiceRepositoryImpl : ServiceRepository {

    override fun findByBusinessId(businessId: Long): List<Service> {
        return list("business.id", businessId)
    }
}
