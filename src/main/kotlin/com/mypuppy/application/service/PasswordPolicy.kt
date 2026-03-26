package com.mypuppy.application.service

import com.mypuppy.domain.exception.WeakPasswordException
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PasswordPolicy {

    fun validate(rawPassword: String) {
        if (rawPassword.length < MIN_LENGTH) {
            throw WeakPasswordException("Password must be at least $MIN_LENGTH characters long")
        }
        if (!rawPassword.any { it.isUpperCase() }) {
            throw WeakPasswordException("Password must contain at least one uppercase letter")
        }
        if (!rawPassword.any { it.isLowerCase() }) {
            throw WeakPasswordException("Password must contain at least one lowercase letter")
        }
        if (!rawPassword.any { it.isDigit() }) {
            throw WeakPasswordException("Password must contain at least one number")
        }
        if (!rawPassword.any { !it.isLetterOrDigit() }) {
            throw WeakPasswordException("Password must contain at least one special character")
        }
    }

    companion object {
        private const val MIN_LENGTH = 8
    }
}
