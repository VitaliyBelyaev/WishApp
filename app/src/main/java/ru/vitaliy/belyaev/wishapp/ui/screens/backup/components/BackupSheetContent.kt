package ru.vitaliy.belyaev.wishapp.ui.screens.backup.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R

@ExperimentalMaterialApi
@Composable
fun BackupSheetContent(
    modifier: Modifier = Modifier,
) {

    Text(
        text = stringResource(R.string.backup_description),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    )
}