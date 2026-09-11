package com.jhonmaxdata.authenticator.data.crypto

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object TotpGenerator {

	private const val TIME_STEP_SECONDS = 30L
	private const val CODE_DIGITS = 6

	fun generateCode(base32Secret: String, timestampMs: Long = System.currentTimeMillis()): String {
		val counter = timestampMs / 1000L / TIME_STEP_SECONDS
		val keyBytes = decodeBase32(base32Secret)

		return generateHmacSha1Otp(keyBytes, counter, CODE_DIGITS)
	}

	fun getRemainingSeconds(timestampMs: Long = System.currentTimeMillis()): Int {
		val seconds = (timestampMs / 1000L) % TIME_STEP_SECONDS
		return (TIME_STEP_SECONDS - seconds).toInt()
	}

	private fun generateHmacSha1Otp(key: ByteArray, counter: Long, digits: Int): String {
		val data = ByteArray(8)
		var value = counter
		for (i in 7 downTo 0) {
			data[i] = (value and 0xFF).toByte()
			value = value ushr 8
		}

		val signKey = SecretKeySpec(key, "RAW")
		val mac = Mac.getInstance("HmacSHA1")
		mac.init(signKey)
		val hash = mac.doFinal(data)

		val offset = hash[hash.size - 1].toInt() and 0x0F
		val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
				((hash[offset + 1].toInt() and 0xFF) shl 16) or
				((hash[offset + 2].toInt() and 0xFF) shl 8) or
				(hash[offset + 3].toInt() and 0xFF)

		val otp = binary % 10.0.pow(digits.toDouble()).toInt()
		return otp.toString().padStart(digits, '0')
	}

	private fun decodeBase32(secret: String): ByteArray {
		val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
		val cleanSecret = secret.uppercase().replace("[^A-Z2-7]".toRegex(), "")

		var buffer = 0
		var bitsLeft = 0
		val result = mutableListOf<Byte>()

		for (char in cleanSecret) {
			val charValue = base32Chars.indexOf(char)
			if (charValue == -1) continue

			buffer = (buffer shl 5) or charValue
			bitsLeft += 5

			if (bitsLeft >= 8) {
				result.add((buffer shr (bitsLeft - 8)).toByte())
				bitsLeft -= 8
			}
		}
		return result.toByteArray()
	}
}
