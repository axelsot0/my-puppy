package com.mypuppy.application.service

import com.mypuppy.domain.exception.DuplicateException
import com.mypuppy.domain.exception.InvalidOperationException
import com.mypuppy.domain.exception.NotFoundException
import com.mypuppy.domain.model.AuthProvider
import com.mypuppy.domain.model.Role
import com.mypuppy.domain.model.User
import com.mypuppy.domain.repository.BusinessRepository
import com.mypuppy.domain.repository.UserRepository
import io.quarkus.elytron.security.common.BcryptUtil
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@ApplicationScoped
class UserService(
    private val userRepository: UserRepository,
    private val businessRepository: BusinessRepository
) {

    fun findById(id: Long): User {
        return userRepository.findById(id)
            ?: throw NotFoundException("User", id)
    }

    fun findByEmailAndBusiness(email: String, businessId: Long): User? {
        return userRepository.findByEmailAndBusinessId(email, businessId)
    }

    fun listByBusinessAndRole(businessId: Long, role: Role): List<User> {
        return userRepository.findByBusinessIdAndRole(businessId, role)
    }

    @Transactional
    fun register(
        businessId: Long,
        email: String,
        firstName: String,
        lastName: String,
        rawPassword: String?,
        authProvider: AuthProvider = AuthProvider.LOCAL,
        providerId: String? = null,
        role: Role = Role.CLIENT
    ): User {
        val business = businessRepository.findById(businessId)
            ?: throw NotFoundException("Business", businessId)

        if (userRepository.findByEmailAndBusinessId(email, businessId) != null) {
            throw DuplicateException("Email '$email' is already registered in this business")
        }

        if (authProvider == AuthProvider.LOCAL && rawPassword.isNullOrBlank()) {
            throw InvalidOperationException("Password is required for local registration")
        }

        val user = User().apply {
            this.email = email
            this.firstName = firstName
            this.lastName = lastName
            this.password = rawPassword?.let { BcryptUtil.bcryptHash(it) }
            this.authProvider = authProvider
            this.providerId = providerId
            this.role = role
            this.business = business
        }

        userRepository.persist(user)
        return user
    }

    @Transactional
    fun update(id: Long, firstName: String?, lastName: String?, rawPassword: String?): User {
        val user = findById(id)

        firstName?.let { user.firstName = it }
        lastName?.let { user.lastName = it }
        rawPassword?.let { user.password = BcryptUtil.bcryptHash(it) }

        return user
    }

    @Transactional
    fun deactivate(id: Long): User {
        val user = findById(id)
        user.active = false
        return user
    }

    fun verifyPassword(user: User, rawPassword: String): Boolean {
        val hash = user.password ?: return false
        return BcryptUtil.matches(rawPassword, hash)
    }
}
