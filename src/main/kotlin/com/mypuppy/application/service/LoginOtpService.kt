package com.mypuppy.application.service

import com.mypuppy.domain.exception.InvalidOperationException
import com.mypuppy.domain.exception.UnauthorizedException
import com.mypuppy.domain.model.LoginOtpChallenge
import com.mypuppy.domain.model.LoginPrincipalType
import com.mypuppy.domain.repository.LoginOtpChallengeRepository
import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.mailer.Mail
import io.quarkus.mailer.Mailer
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class LoginOtpService(
    private val loginOtpChallengeRepository: LoginOtpChallengeRepository,
    private val mailer: Mailer,
    @ConfigProperty(name = "mypuppy.otp.expiration-minutes", defaultValue = "5")
    private val otpExpirationMinutes: Long,
    @ConfigProperty(name = "mypuppy.otp.max-attempts", defaultValue = "5")
    private val maxAttempts: Int,
    @ConfigProperty(name = "mypuppy.otp.mail.from", defaultValue = "no-reply@mypuppy.com")
    private val otpMailFrom: String,
    @ConfigProperty(name = "mypuppy.otp.mail.subject", defaultValue = "Your My Puppy OTP code")
    private val otpMailSubject: String
) {

    @Transactional
    fun createLoginChallenge(
        email: String,
        principalId: UUID,
        principalType: LoginPrincipalType,
        businessId: UUID?
    ): LoginOtpChallenge {
        val otp = generateOtpCode()
        val challenge = LoginOtpChallenge().apply {
            this.email = email
            this.principalId = principalId
            this.principalType = principalType
            this.businessId = businessId
            this.otpHash = BcryptUtil.bcryptHash(otp)
            this.expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes)
            this.attempts = 0
            this.maxAttempts = maxAttempts
        }

        loginOtpChallengeRepository.persist(challenge)
        sendOtpEmail(email, otp, principalType)
        return challenge
    }

    @Transactional
    fun verifyChallenge(
        challengeId: UUID,
        otp: String,
        principalType: LoginPrincipalType,
        businessId: UUID? = null
    ): LoginOtpChallenge {
        val challenge = loginOtpChallengeRepository.findById(challengeId)
            ?: throw UnauthorizedException("Invalid OTP challenge")

        if (challenge.principalType != principalType) {
            throw UnauthorizedException("Invalid OTP challenge")
        }

        if (principalType == LoginPrincipalType.USER && challenge.businessId != businessId) {
            throw UnauthorizedException("Invalid OTP challenge")
        }

        if (challenge.consumedAt != null) {
            throw InvalidOperationException("OTP already used")
        }

        if (LocalDateTime.now().isAfter(challenge.expiresAt)) {
            throw InvalidOperationException("OTP expired")
        }

        if (challenge.attempts >= challenge.maxAttempts) {
            throw InvalidOperationException("OTP attempts exceeded")
        }

        if (!BcryptUtil.matches(otp, challenge.otpHash)) {
            challenge.attempts += 1
            throw UnauthorizedException("Invalid OTP")
        }

        challenge.consumedAt = LocalDateTime.now()
        return challenge
    }

    fun otpExpirationSeconds(): Long = otpExpirationMinutes * 60

    private fun sendOtpEmail(email: String, otp: String, principalType: LoginPrincipalType) {
        val recipientLabel = if (principalType == LoginPrincipalType.SUPER_ADMIN) "super admin" else "user"
        val html = """
            <html>
              <body style="font-family: Arial, sans-serif; color: #111;">
                <h2>My Puppy - Login OTP</h2>
                <p>Hello $recipientLabel,</p>
                <p>Your one-time code is:</p>
                <p style="font-size: 28px; font-weight: bold; letter-spacing: 4px;">$otp</p>
                <p>This code expires in $otpExpirationMinutes minutes.</p>
              </body>
            </html>
        """.trimIndent()

        mailer.send(
            Mail.withHtml(email, otpMailSubject, html).setFrom(otpMailFrom)
        )
    }

    private fun generateOtpCode(): String {
        val value = secureRandom.nextInt(1_000_000)
        return String.format("%06d", value)
    }

    companion object {
        private val secureRandom = SecureRandom()
    }
}
