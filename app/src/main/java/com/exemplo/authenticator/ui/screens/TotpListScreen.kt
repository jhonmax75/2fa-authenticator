package com.jhonmaxdata.authenticator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jhonmaxdata.authenticator.ui.TotpViewModel
import com.jhonmaxdata.authenticator.ui.components.TotpItemCard

@Composable
fun TotpListScreen(
	viewModel: TotpViewModel,
	onAddAccount: () -> Unit,
	modifier: Modifier = Modifier
) {
	val accounts by viewModel.accounts.collectAsStateWithLifecycle()
	var accountToDelete by remember { mutableStateOf<Long?>(null) }

	Scaffold(
		modifier = modifier.fillMaxSize(),
		floatingActionButton = {
			FloatingActionButton(onClick = onAddAccount) {
				Icon(
					imageVector = Icons.Default.Add,
					contentDescription = "Adicionar conta"
				)
			}
		}
	) { innerPadding ->
		if (accounts.isEmpty()) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding),
				contentAlignment = Alignment.Center
			) {
				Text(
					text = "Nenhuma conta adicionada.",
					modifier = Modifier.padding(24.dp)
				)
			}
		} else {
			LazyColumn(
				modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding),
				contentPadding = PaddingValues(16.dp),
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				items(
					items = accounts,
					key = { it.account.id }
				) { accountState ->
					TotpItemCard(
						state = accountState,
						onDelete = {
							accountToDelete = accountState.account.id
						},
						modifier = Modifier.fillMaxWidth()
					)
				}
			}
		}
	}

	if (accountToDelete != null) {
		AlertDialog(
			onDismissRequest = {
				accountToDelete = null
			},
			title = {
				Text("Excluir conta?")
			},
			text = {
				Text("Esta conta será removida do autenticador.")
			},
			confirmButton = {
				TextButton(
					onClick = {
						accountToDelete?.let { id ->
							viewModel.deleteAccount(id)
						}
						accountToDelete = null
					}
				) {
					Text("Excluir")
				}
			},
			dismissButton = {
				TextButton(
					onClick = {
						accountToDelete = null
					}
				) {
					Text("Cancelar")
				}
			}
		)
	}
}
