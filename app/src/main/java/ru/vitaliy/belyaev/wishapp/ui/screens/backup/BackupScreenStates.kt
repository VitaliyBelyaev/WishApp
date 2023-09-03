package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo

sealed class LoadingState {
    object None : LoadingState()
    object Empty : LoadingState()
    object CheckingBackup : LoadingState()
    object UploadingNewBackup : LoadingState()
    object RestoringBackup : LoadingState()
}

sealed class BackupViewState {
    object None : BackupViewState()
    object DrivePermissionRationale : BackupViewState()
    object CheckBackupError : BackupViewState()
    object NoBackup : BackupViewState()

    sealed class CurrentBackup : BackupViewState() {

        abstract val backupInfo: BackupInfo.Value

        data class WithRestore(override val backupInfo: BackupInfo.Value) : CurrentBackup()
        data class WithForceUpdate(override val backupInfo: BackupInfo.Value) : CurrentBackup()
    }
}