package com.mypuppy.application.service

import com.mypuppy.domain.exception.DuplicateException
import com.mypuppy.domain.exception.NotFoundException
import com.mypuppy.domain.model.Business
import com.mypuppy.domain.repository.BusinessRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.UUID

@ApplicationScoped
class BusinessService(
    private val businessRepository: BusinessRepository
) {

    fun findById(id: UUID): Business {
        return businessRepository.findById(id)
            ?: throw NotFoundException("Business", id)
    }

    fun listActive(): List<Business> {
        return businessRepository.findByActive(true)
    }

    fun listAll(): List<Business> {
        return businessRepository.listAll()
    }

    @Transactional
    fun create(name: String, slug: String, type: String, description: String?, address: String?, phone: String?): Business {
        if (businessRepository.findBySlug(slug) != null) {
            throw DuplicateException("A business with slug '$slug' already exists")
        }

        val business = Business().apply {
            this.name = name
            this.slug = slug
            this.type = type
            this.description = description
            this.address = address
            this.phone = phone
        }

        businessRepository.persist(business)
        return business
    }

    @Transactional
    fun update(id: UUID, name: String?, type: String?, description: String?, address: String?, phone: String?): Business {
        val business = findById(id)

        name?.let { business.name = it }
        type?.let { business.type = it }
        description?.let { business.description = it }
        address?.let { business.address = it }
        phone?.let { business.phone = it }

        return business
    }

    @Transactional
    fun deactivate(id: UUID): Business {
        val business = findById(id)
        business.active = false
        return business
    }

    @Transactional
    fun activate(id: UUID): Business {
        val business = findById(id)
        business.active = true
        return business
    }
}
