package com.jhonmaxdata.authenticator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhonmaxdata.authenticator.data.crypto.TotpGenerator
import com.jhonmaxdata.authenticator.data.otp.OtpAuthData
import com.jhonmaxdata.authenticator.data.repository.Account
import com.jhonmaxdata.authenticator.data.repository.AccountRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TotpAccountState(
	val account: Account,
	val code: String,
	val remainingSeconds: Int
)

class TotpViewModel(
	private val repository: AccountRepository
) : ViewModel() {

	private val _accounts = MutableStateFlow<List<TotpAccountState>>(emptyList())
	val accounts: StateFlow<List<TotpAccountState>> = _accounts.asStateFlow()

	init {
		observeAccounts()
		startCodeRefresh()
	}

	private fun observeAccounts() {
		viewModelScope.launch {
			repository.allAccounts.collect { accounts ->
				updateAccounts(accounts)
			}
		}
	}

	private fun startCodeRefresh() {
		viewModelScope.launch {
			while (true) {
				updateAccounts(_accounts.value.map { it.account })
				delay(1000L)
			}
		}
	}

	private fun updateAccounts(accounts: List<Account>) {
		val now = System.currentTimeMillis()

		_accounts.value = accounts.map { account ->
			TotpAccountState(
				account = account,
				code = TotpGenerator.generateCode(
					base32Secret = account.secret,
					timestampMs = now
				),
				remainingSeconds = TotpGenerator.getRemainingSeconds(now)
			)
		}
	}

	fun addAccount(
		issuer: String,
		accountName: String,
		secret: String
	) {
		viewModelScope.launch {
			repository.addAccount(
				issuer = issuer,
				accountName = accountName,
				rawSecret = secret
			)
		}
	}

	fun addAccount(data: OtpAuthData) {
		addAccount(
			issuer = data.issuer,
			accountName = data.accountName,
			secret = data.secret
		)
	}

	fun deleteAccount(accountId: Long) {
		viewModelScope.launch {
			repository.deleteAccountById(accountId)
		}
	}
}
