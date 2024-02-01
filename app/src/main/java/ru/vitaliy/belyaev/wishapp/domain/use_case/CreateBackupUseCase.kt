package ru.vitaliy.belyaev.wishapp.domain.use_case

import java.io.File
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupLoadProgressListener

internal class CreateBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val dataStoreRepository: DataStoreRepository
) {

    suspend operator fun invoke(
        backupFile: File,
        progressListener: BackupLoadProgressListener,
    ): BackupInfo.Value {
        val backupInfo = backupRepository.uploadNewBackup(
            backupFile = backupFile,
            progressListener = progressListener
        )
        dataStoreRepository.updateIsBackupWasCreated(true)
        return backupInfo
    }
}