package com.mypuppy.infrastructure.scheduler

import com.mypuppy.domain.repository.LoginOtpChallengeRepository
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.jboss.logging.Logger
import java.time.LocalDateTime

@ApplicationScoped
class OtpCleanupJob(
    private val loginOtpChallengeRepository: LoginOtpChallengeRepository
) {

    private val log = Logger.getLogger(OtpCleanupJob::class.java)

    @Scheduled(cron = "\${mypuppy.otp.cleanup.cron:0 0 * * * ?}")
    @Transactional
    fun cleanupExpiredOtps() {
        val cutoff = LocalDateTime.now().minusHours(1)
        val deleted = loginOtpChallengeRepository.deleteExpiredBefore(cutoff)
        if (deleted > 0) {
            log.infof("OTP cleanup: removed %d expired challenges", deleted)
        }
    }
}
