package com.mypuppy.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "login_otp_challenges")
class LoginOtpChallenge : BaseEntity() {

    @Column(nullable = false)
    var email: String = ""

    @Column(name = "principal_id", columnDefinition = "uuid", nullable = false)
    var principalId: UUID = UUID.randomUUID()

    @Enumerated(EnumType.STRING)
    @Column(name = "principal_type", nullable = false)
    var principalType: LoginPrincipalType = LoginPrincipalType.USER

    @Column(name = "business_id", columnDefinition = "uuid")
    var businessId: UUID? = null

    @Column(name = "otp_hash", nullable = false)
    var otpHash: String = ""

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "consumed_at")
    var consumedAt: LocalDateTime? = null

    @Column(nullable = false)
    var attempts: Int = 0

    @Column(name = "max_attempts", nullable = false)
    var maxAttempts: Int = 5
}
