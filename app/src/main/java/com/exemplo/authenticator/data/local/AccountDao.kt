package com.jhonmaxdata.authenticator.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

	@Query("SELECT * FROM accounts ORDER BY issuer ASC, accountName ASC")
	fun getAllAccounts(): Flow<List<AccountEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAccount(account: AccountEntity)

	@Delete
	suspend fun deleteAccount(account: AccountEntity)

	@Query("DELETE FROM accounts WHERE id = :accountId")
	suspend fun deleteById(accountId: Long)
}
