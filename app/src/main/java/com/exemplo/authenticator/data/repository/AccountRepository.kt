package com.jhonmaxdata.authenticator.data.repository

import com.jhonmaxdata.authenticator.data.crypto.AndroidKeystoreManager
import com.jhonmaxdata.authenticator.data.local.AccountDao
import com.jhonmaxdata.authenticator.data.local.AccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Account(
	val id: Long = 0,
	val issuer: String,
	val accountName: String,
	val secret: String
)

class AccountRepository(private val accountDao: AccountDao) {

	val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts().map { entities ->
		entities.map { entity ->
			val plainSecret = AndroidKeystoreManager.decrypt(
				encryptedData = entity.encryptedSecret,
				iv = entity.iv
			)
			Account(
				id = entity.id,
				issuer = entity.issuer,
				accountName = entity.accountName,
				secret = plainSecret
			)
		}
	}

	suspend fun addAccount(issuer: String, accountName: String, rawSecret: String) {
		val (encryptedSecret, iv) = AndroidKeystoreManager.encrypt(rawSecret)
		val entity = AccountEntity(
			issuer = issuer,
			accountName = accountName,
			encryptedSecret = encryptedSecret,
			iv = iv
		)
		accountDao.insertAccount(entity)
	}

	suspend fun deleteAccountById(id: Long) {
		accountDao.deleteById(id)
	}
}
