package com.mypuppy.application.service

import com.mypuppy.domain.exception.NotFoundException
import com.mypuppy.domain.model.Service
import com.mypuppy.domain.repository.BusinessRepository
import com.mypuppy.domain.repository.ServiceRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.math.BigDecimal

@ApplicationScoped
class ServiceService(
    private val serviceRepository: ServiceRepository,
    private val businessRepository: BusinessRepository
) {

    fun findById(id: Long): Service {
        return serviceRepository.findById(id)
            ?: throw NotFoundException("Service", id)
    }

    fun findByBusinessId(businessId: Long): List<Service> {
        return serviceRepository.findByBusinessId(businessId)
    }

    @Transactional
    fun create(businessId: Long, name: String, description: String?, price: BigDecimal, durationMinutes: Int): Service {
        val business = businessRepository.findById(businessId)
            ?: throw NotFoundException("Business", businessId)

        val service = Service().apply {
            this.business = business
            this.name = name
            this.description = description
            this.price = price
            this.durationMinutes = durationMinutes
        }

        serviceRepository.persist(service)
        return service
    }

    @Transactional
    fun update(id: Long, name: String?, description: String?, price: BigDecimal?, durationMinutes: Int?): Service {
        val service = findById(id)

        name?.let { service.name = it }
        description?.let { service.description = it }
        price?.let { service.price = it }
        durationMinutes?.let { service.durationMinutes = it }

        return service
    }

    @Transactional
    fun deactivate(id: Long): Service {
        val service = findById(id)
        service.active = false
        return service
    }
}
