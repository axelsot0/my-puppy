package com.mypuppy.infrastructure.persistence

import com.mypuppy.domain.repository.LoginOtpChallengeRepository
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime

@ApplicationScoped
class LoginOtpChallengeRepositoryImpl : LoginOtpChallengeRepository {

    override fun countRecentByEmail(email: String, since: LocalDateTime): Long {
        return count("email = ?1 and createdAt >= ?2", email, since)
    }

    override fun deleteExpiredBefore(cutoff: LocalDateTime): Long {
        return delete("expiresAt < ?1", cutoff)
    }
}
