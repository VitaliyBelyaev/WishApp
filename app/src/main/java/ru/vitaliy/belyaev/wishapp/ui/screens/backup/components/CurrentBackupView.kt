package ru.vitaliy.belyaev.wishapp.ui.screens.backup.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupDateTimeFormatter
import ru.vitaliy.belyaev.wishapp.utils.BytesSizeFormatter

@Composable
internal fun CurrentBackupView(
    backupInfo: BackupInfo = BackupInfo.None(),
    onCreateBackupClicked: () -> Unit,
    onRefreshBackupInfoClicked: () -> Unit
) {

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.backup_current_backup_info_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .weight(6f)
            )

            if (backupInfo is BackupInfo.Value) {
                FilledTonalIconButton(
                    modifier = Modifier
                        .size(26.dp)
                        .weight(1f),
                    onClick = onRefreshBackupInfoClicked
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }

        when (backupInfo) {
            is BackupInfo.None -> {
                ContentText(
                    text = stringResource(R.string.backup_no_backup_text),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            is BackupInfo.Value -> {
                backupInfo.accountEmail?.let {
                    ContentText(
                        text = stringResource(R.string.backup_current_backup_info_account, it),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                ContentText(
                    text = stringResource(
                        R.string.backup_current_backup_info_date,
                        BackupDateTimeFormatter.formatBackupDateTime(backupInfo.modifiedDateTime)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ContentText(
                    text = stringResource(
                        R.string.backup_current_backup_info_size,
                        BytesSizeFormatter.format(backupInfo.sizeInBytes)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (backupInfo.device != null) {
                    ContentText(
                        text = stringResource(R.string.backup_current_backup_info_device, backupInfo.device),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
        Button(onClick = { onCreateBackupClicked() }) {
            Text(stringResource(R.string.backup_create_backup_button_text))
        }
    }
}