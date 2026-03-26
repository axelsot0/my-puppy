package com.mypuppy.domain.repository

import com.mypuppy.domain.model.LoginOtpChallenge
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import java.time.LocalDateTime
import java.util.UUID

interface LoginOtpChallengeRepository : PanacheRepositoryBase<LoginOtpChallenge, UUID> {
    fun countRecentByEmail(email: String, since: LocalDateTime): Long
    fun deleteExpiredBefore(cutoff: LocalDateTime): Long
}
