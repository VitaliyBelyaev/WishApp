package ru.vitaliy.belyaev.wishapp.ui.screens.backup.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.loader.FullscreenLoaderWithText
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.LoadingState

@Composable
internal fun LoadingView(loadingState: LoadingState) {
    when (loadingState) {
        is LoadingState.None -> {
        }

        is LoadingState.Empty -> {
            FullscreenLoaderWithText(isTranslucent = false)
        }

        is LoadingState.CheckingBackup -> {
            FullscreenLoaderWithText(
                isTranslucent = true,
                text = stringResource(R.string.backup_check_backup_loader_text),
            )
        }

        is LoadingState.RestoringBackup -> {
            when (loadingState) {
                is LoadingState.RestoringBackup.Indeterminate -> {
                    FullscreenLoaderWithText(
                        isTranslucent = true,
                        text = stringResource(R.string.backup_restore_backup_loader_text),
                    )
                }
                is LoadingState.RestoringBackup.Determinate -> {
                    FullscreenLoaderWithText(
                        isTranslucent = true,
                        text = stringResource(R.string.backup_restore_backup_loader_text),
                        progress = loadingState.progress,
                    )
                }
            }
        }

        is LoadingState.UploadingNewBackup -> {
            when (loadingState) {
                is LoadingState.UploadingNewBackup.Indeterminate -> {
                    FullscreenLoaderWithText(
                        isTranslucent = true,
                        text = stringResource(R.string.backup_create_backup_loader_text),
                    )
                }
                is LoadingState.UploadingNewBackup.Determinate -> {
                    FullscreenLoaderWithText(
                        isTranslucent = true,
                        text = stringResource(R.string.backup_create_backup_loader_text),
                        progress = loadingState.progress,
                    )
                }
            }
        }
    }
}