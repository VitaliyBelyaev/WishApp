package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo

sealed class LoadingState {
    data object None : LoadingState()
    data object Empty : LoadingState()
    data object CheckingBackup : LoadingState()
    sealed class UploadingNewBackup : LoadingState() {

        data object Indeterminate : UploadingNewBackup()
        data class Determinate(val progress: Progress) : UploadingNewBackup()
    }

    sealed class RestoringBackup : LoadingState() {

        data object Indeterminate : RestoringBackup()
        data class Determinate(val progress: Progress) : RestoringBackup()
    }
}

/**
 * [progress] - from 0.0 to 1.0
 * [progressCurrentToTotalString] - string like "1.5 MB / 2.0 MB"
 */
data class Progress(
    val progress: Double,
    val progressCurrentToTotalString: String,
)

sealed class BackupViewState {
    data object None : BackupViewState()
    data object DrivePermissionRationale : BackupViewState()
    data object CheckBackupError : BackupViewState()
    data class NoBackup(val backupInfo: BackupInfo.None) : BackupViewState()

    sealed class CurrentBackup : BackupViewState() {

        abstract val backupInfo: BackupInfo.Value

        data class WithRestore(override val backupInfo: BackupInfo.Value) : CurrentBackup()
        data class WithForceUpdate(override val backupInfo: BackupInfo.Value) : CurrentBackup()
    }
}