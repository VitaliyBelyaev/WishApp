package ru.vitaliy.belyaev.wishapp.ui.screens.backup.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.loader.FullscreenLoaderWithText
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.LoadingState

@Composable
internal fun LoadingView(loadingState: LoadingState) {
    when (loadingState) {
        LoadingState.None -> {
        }

        LoadingState.Empty -> {
            FullscreenLoaderWithText(isTranslucent = false)
        }

        LoadingState.CheckingBackup -> {
            FullscreenLoaderWithText(
                isTranslucent = true,
                text = stringResource(R.string.backup_check_backup_loader_text),
            )
        }

        LoadingState.RestoringBackup -> {
            FullscreenLoaderWithText(
                isTranslucent = true,
                text = stringResource(R.string.backup_restore_backup_loader_text),
            )
        }

        LoadingState.UploadingNewBackup -> {
            FullscreenLoaderWithText(
                isTranslucent = true,
                text = stringResource(R.string.backup_create_backup_loader_text),
            )
        }
    }
}