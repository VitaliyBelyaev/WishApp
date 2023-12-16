package ru.vitaliy.belyaev.wishapp.ui.screens.backup.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R

@Composable
internal fun DrivePermissionRationaleView(onGiveDrivePermissionClicked: () -> Unit) {

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.backup_drive_permission_rationale_text),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(onClick = onGiveDrivePermissionClicked) {
            Text(stringResource(R.string.backup_allow_access_to_drive_text))
        }
    }
}