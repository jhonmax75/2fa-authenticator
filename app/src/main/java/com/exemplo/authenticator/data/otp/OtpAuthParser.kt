package com.jhonmaxdata.authenticator.data.otp

import android.net.Uri

data class OtpAuthData(
    val issuer: String,
    val accountName: String,
    val secret: String
)

object OtpAuthParser {

    fun parse(uriString: String): OtpAuthData? {
        val uri = try {
            Uri.parse(uriString)
        } catch (exception: Exception) {
            return null
        }

        if (uri.scheme != "otpauth" || uri.host != "totp") {
            return null
        }

        val secret = uri.getQueryParameter("secret")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        val label = uri.pathSegments.firstOrNull()
            ?.let { Uri.decode(it) }
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        val issuerParameter = uri.getQueryParameter("issuer")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        val accountName: String
        val issuer: String

        if (label.contains(":")) {
            val parts = label.split(":", limit = 2)
            issuer = issuerParameter ?: parts[0].trim()
            accountName = parts[1].trim()
        } else {
            issuer = issuerParameter ?: ""
            accountName = label
        }

        if (accountName.isEmpty()) {
            return null
        }

        return OtpAuthData(
            issuer = issuer,
            accountName = accountName,
            secret = secret
        )
    }
}
