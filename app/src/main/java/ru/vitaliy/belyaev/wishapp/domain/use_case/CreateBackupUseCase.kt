package ru.vitaliy.belyaev.wishapp.domain.use_case

import java.io.File
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.CurrentBackupInfoHolder
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupLoadProgressListener
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository

internal class CreateBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val currentBackupInfoHolder: CurrentBackupInfoHolder,
) {

    suspend operator fun invoke(
        backupFile: File,
        progressListener: BackupLoadProgressListener,
    ): BackupInfo.Value {
        val backupInfo = backupRepository.uploadNewBackup(
            backupFile = backupFile,
            progressListener = progressListener,
        )
        dataStoreRepository.updateIsBackupWasCreated(true)
        // Здесь, а не в экране: ушли с экрана посреди загрузки — копия всё равно записана, и при
        // следующем открытии экран без проверки покажет её, а не прежний ответ
        currentBackupInfoHolder.updateBackupInfo(backupInfo)
        return backupInfo
    }
}
