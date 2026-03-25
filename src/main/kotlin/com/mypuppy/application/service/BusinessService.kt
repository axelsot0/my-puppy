package com.mypuppy.application.service

import com.mypuppy.domain.exception.NotFoundException
import com.mypuppy.domain.model.Business
import com.mypuppy.domain.repository.BusinessRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@ApplicationScoped
class BusinessService(
    private val businessRepository: BusinessRepository
) {

    fun findById(id: Long): Business {
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
    fun update(id: Long, name: String?, type: String?, description: String?, address: String?, phone: String?): Business {
        val business = findById(id)

        name?.let { business.name = it }
        type?.let { business.type = it }
        description?.let { business.description = it }
        address?.let { business.address = it }
        phone?.let { business.phone = it }

        return business
    }

    @Transactional
    fun deactivate(id: Long): Business {
        val business = findById(id)
        business.active = false
        return business
    }

    @Transactional
    fun activate(id: Long): Business {
        val business = findById(id)
        business.active = true
        return business
    }
}
