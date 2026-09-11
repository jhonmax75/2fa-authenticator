package com.jhonmaxdata.authenticator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val issuer: String,
	val accountName: String,
	val encryptedSecret: ByteArray,
	val iv: ByteArray
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as AccountEntity

		if (id != other.id) return false
		if (issuer != other.issuer) return false
		if (accountName != other.accountName) return false
		if (!encryptedSecret.contentEquals(other.encryptedSecret)) return false
		if (!iv.contentEquals(other.iv)) return false

		return true
	}

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + issuer.hashCode()
		result = 31 * result + accountName.hashCode()
		result = 31 * result + encryptedSecret.contentHashCode()
		result = 31 * result + iv.contentHashCode()
		return result
	}
}
