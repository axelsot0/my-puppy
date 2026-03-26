package com.mypuppy.infrastructure.mail

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@ApplicationScoped
class EmailService(
    @ConfigProperty(name = "mypuppy.mail.provider", defaultValue = "mock")
    private val provider: String,
    @ConfigProperty(name = "mypuppy.mail.from", defaultValue = "onboarding@resend.dev")
    private val mailFrom: String,
    @ConfigProperty(name = "mypuppy.mail.resend-api-key", defaultValue = "")
    private val resendApiKey: String
) {

    private val httpClient = HttpClient.newHttpClient()

    fun sendAsync(to: String, subject: String, html: String) {
        if (provider == "mock") {
            log.infof("[MOCK EMAIL] to=%s subject=%s", to, subject)
            return
        }

        Thread.startVirtualThread {
            try {
                val body = """
                    {
                      "from": "$mailFrom",
                      "to": ["$to"],
                      "subject": "$subject",
                      "html": ${escapeJson(html)}
                    }
                """.trimIndent()

                val request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer $resendApiKey")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build()

                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() in 200..299) {
                    log.infof("Email sent to %s via Resend", to)
                } else {
                    log.errorf("Resend API error [%d]: %s", response.statusCode(), response.body())
                }
            } catch (e: Exception) {
                log.errorf(e, "Failed to send email to %s", to)
            }
        }
    }

    private fun escapeJson(value: String): String {
        return "\"" + value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t") + "\""
    }

    companion object {
        private val log = Logger.getLogger(EmailService::class.java)
    }
}
