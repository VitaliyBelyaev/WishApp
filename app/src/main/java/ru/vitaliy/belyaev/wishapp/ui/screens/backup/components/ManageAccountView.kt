package ru.vitaliy.belyaev.wishapp.ui.screens.backup.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R

@Composable
internal fun ManageAccountView(
    accountEmail: String?,
    onSignOutClicked: () -> Unit,
    onDisconnectAccountClicked: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.backup_account_manage_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (!accountEmail.isNullOrBlank()) {
            ContentText(
                text = "$accountEmail",
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        OutlinedButton(onClick = onSignOutClicked) {
            Text(text = stringResource(R.string.backup_account_sing_out_button_text))
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        ContentText(
            text = stringResource(R.string.backup_account_disconnect_description),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedButton(onClick = onDisconnectAccountClicked) {
            Text(text = stringResource(R.string.backup_account_disconnect_button_text))
        }
    }
}