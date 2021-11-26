package ru.vitaliy.belyaev.wishapp.ui.screens.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R

@Composable
fun BackupSheetContent() {

    Text(
        text = stringResource(R.string.backup_description),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp)
    )
}