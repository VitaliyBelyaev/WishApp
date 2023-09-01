package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo

sealed class LoadingState {
    object None : LoadingState()
    object Empty : LoadingState()
    object OverData : LoadingState()
}

sealed class BackupViewState {
    object None : BackupViewState()
    object DrivePermissionRationale : BackupViewState()
    object CheckBackupError : BackupViewState()
    object NoBackup : BackupViewState()
    data class CurrentBackupWithRestore(val backupInfo: BackupInfo.Value) : BackupViewState()
    data class CurrentBackup(val backupInfo: BackupInfo.Value) : BackupViewState()
}