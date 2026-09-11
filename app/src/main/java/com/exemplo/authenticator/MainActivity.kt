package com.jhonmaxdata.authenticator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jhonmaxdata.authenticator.data.local.AppDatabase
import com.jhonmaxdata.authenticator.data.repository.AccountRepository
import com.jhonmaxdata.authenticator.ui.TotpViewModel
import com.jhonmaxdata.authenticator.ui.screens.QrScannerScreen
import com.jhonmaxdata.authenticator.ui.screens.TotpListScreen
import com.jhonmaxdata.authenticator.ui.theme.AuthenticatorTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			AuthenticatorTheme {
				AuthenticatorContent()
			}
		}
	}
}

@Composable
private fun AuthenticatorContent() {
	val context = androidx.compose.ui.platform.LocalContext.current
	val database = AppDatabase.getDatabase(context)
	val repository = AccountRepository(database.accountDao())
	val factory = TotpViewModelFactory(repository)
	val viewModel: TotpViewModel = viewModel(factory = factory)
	var showScanner by remember { mutableStateOf(false) }

	if (showScanner) {
		QrScannerScreen(
			onQrCodeScanned = { rawValue ->
				val data = com.jhonmaxdata.authenticator.data.otp.OtpAuthParser.parse(rawValue)

				if (data != null) {
					viewModel.addAccount(data)
					showScanner = false
				}
			},
			onBackClicked = {
				showScanner = false
			}
		)
	} else {
		TotpListScreen(
			viewModel = viewModel,
			onAddAccount = {
				showScanner = true
			}
		)
	}
}

private class TotpViewModelFactory(
	private val repository: AccountRepository
) : ViewModelProvider.Factory {

	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(TotpViewModel::class.java)) {
			return TotpViewModel(repository) as T
		}

		throw IllegalArgumentException(
			"Unknown ViewModel class: ${modelClass.name}"
		)
	}
}
