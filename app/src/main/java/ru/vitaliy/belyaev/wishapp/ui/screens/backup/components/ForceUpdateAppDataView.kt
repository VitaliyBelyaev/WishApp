package ru.vitaliy.belyaev.wishapp.ui.screens.backup.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.theme.AppButtonDefaults

@Composable
internal fun ForceUpdateAppDataView(onForceUpdateClicked: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.backup_restore_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ContentText(
            text = stringResource(R.string.backup_force_update_info_text),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedButton(
            onClick = onForceUpdateClicked,
            shape = AppButtonDefaults.defaultButtonShape(),
        ) {
            Text(text = stringResource(R.string.backup_force_update_button_text))
        }
    }
}