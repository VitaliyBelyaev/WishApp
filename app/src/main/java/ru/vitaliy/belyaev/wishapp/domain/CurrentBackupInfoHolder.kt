package ru.vitaliy.belyaev.wishapp.domain

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo

@Singleton
internal class CurrentBackupInfoHolder @Inject constructor() {

    private val _backupInfo: MutableStateFlow<BackupInfo> = MutableStateFlow(BackupInfo.None())
    val backupInfo: StateFlow<BackupInfo> = _backupInfo.asStateFlow()

    fun updateBackupInfo(backupInfo: BackupInfo) {
        _backupInfo.value = backupInfo
    }
}