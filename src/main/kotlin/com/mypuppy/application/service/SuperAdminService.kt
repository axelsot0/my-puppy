package com.mypuppy.application.service

import com.mypuppy.domain.exception.DuplicateException
import com.mypuppy.domain.exception.InvalidOperationException
import com.mypuppy.domain.exception.NotFoundException
import com.mypuppy.domain.model.SuperAdmin
import com.mypuppy.domain.repository.SuperAdminRepository
import io.quarkus.elytron.security.common.BcryptUtil
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.UUID

@ApplicationScoped
class SuperAdminService(
    private val superAdminRepository: SuperAdminRepository
) {

    fun listAll(): List<SuperAdmin> = superAdminRepository.listAll()

    @Transactional
    fun create(email: String, password: String, name: String): SuperAdmin {
        if (superAdminRepository.findByEmail(email) != null) {
            throw DuplicateException("SuperAdmin with email '$email' already exists")
        }

        val admin = SuperAdmin().apply {
            this.email = email
            this.password = BcryptUtil.bcryptHash(password)
            this.name = name
        }
        superAdminRepository.persist(admin)
        return admin
    }

    @Transactional
    fun delete(id: UUID, currentAdminId: UUID) {
        if (id == currentAdminId) {
            throw InvalidOperationException("Cannot delete yourself")
        }

        val admin = superAdminRepository.findById(id)
            ?: throw NotFoundException("SuperAdmin", id)

        superAdminRepository.delete(admin)
    }
}
