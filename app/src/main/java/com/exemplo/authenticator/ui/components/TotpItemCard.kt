package com.jhonmaxdata.authenticator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jhonmaxdata.authenticator.ui.TotpAccountState

@Composable
fun TotpItemCard(
	state: TotpAccountState,
	onDelete: () -> Unit,
	modifier: Modifier = Modifier
) {
	Card(
		modifier = modifier.fillMaxWidth()
	) {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text(
				text = state.account.issuer,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold
			)

			Text(
				text = state.account.accountName,
				style = MaterialTheme.typography.bodyMedium
			)

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				Text(
					text = state.code,
					style = MaterialTheme.typography.headlineMedium,
					fontWeight = FontWeight.Bold
				)

				Row {
					Text(
						text = "${state.remainingSeconds}s",
						style = MaterialTheme.typography.bodyLarge
					)

					IconButton(
						onClick = onDelete
					) {
						Icon(
							imageVector = Icons.Default.Delete,
							contentDescription = "Excluir conta"
						)
					}
				}
			}
		}
	}
}
