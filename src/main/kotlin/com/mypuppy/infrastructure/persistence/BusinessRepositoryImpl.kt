package com.mypuppy.infrastructure.persistence

import com.mypuppy.domain.model.Business
import com.mypuppy.domain.repository.BusinessRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class BusinessRepositoryImpl : BusinessRepository {

    override fun findByActive(active: Boolean): List<Business> {
        return list("active", active)
    }

    override fun findBySlug(slug: String): Business? {
        return find("slug", slug).firstResult()
    }
}
