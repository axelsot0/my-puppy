package com.mypuppy.infrastructure.seed

import com.mypuppy.domain.model.SuperAdmin
import com.mypuppy.domain.repository.SuperAdminRepository
import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class DataSeeder(
    private val superAdminRepository: SuperAdminRepository,
    @ConfigProperty(name = "mypuppy.super-admin.email")
    private val adminEmail: String,
    @ConfigProperty(name = "mypuppy.super-admin.password")
    private val adminPassword: String,
    @ConfigProperty(name = "mypuppy.super-admin.name", defaultValue = "Platform Admin")
    private val adminName: String
) {

    @Transactional
    fun onStart(@Observes event: StartupEvent) {
        if (superAdminRepository.findByEmail(adminEmail) == null) {
            val admin = SuperAdmin().apply {
                email = adminEmail
                password = BcryptUtil.bcryptHash(adminPassword)
                name = adminName
            }
            superAdminRepository.persist(admin)
        }
    }
}
