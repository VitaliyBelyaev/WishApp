package ru.vitaliy.belyaev.wishapp.domain.use_case

import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.domain.CurrentBackupInfoHolder
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository

internal class GetExistingBackupInfoUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val currentBackupInfoHolder: CurrentBackupInfoHolder,
) {

    suspend operator fun invoke(forceRemote: Boolean): BackupInfo {
        val localBackupInfo = currentBackupInfoHolder.backupInfo.value
        return when {
            !forceRemote && localBackupInfo is BackupInfo.Value -> localBackupInfo
            else -> getFromRemoteAndUpdateHolder()
        }
    }

    private suspend fun getFromRemoteAndUpdateHolder(): BackupInfo {
        val backupInfo = backupRepository.checkExistingBackup()
        currentBackupInfoHolder.updateBackupInfo(backupInfo)
        return backupInfo
    }
}