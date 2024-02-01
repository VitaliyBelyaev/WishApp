package ru.vitaliy.belyaev.wishapp.domain.use_case

import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupLoadProgressListener

internal class RestoreBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val dataStoreRepository: DataStoreRepository
) {

    suspend operator fun invoke(
        backupFileId: String,
        fileWhereCurrentDbStored: File,
        progressListener: BackupLoadProgressListener,
    ) {

        val outputStream = ByteArrayOutputStream()
        backupRepository.downloadBackup(
            fileId = backupFileId,
            outputStream = outputStream,
            progressListener = progressListener
        )

        fileWhereCurrentDbStored.delete()
        fileWhereCurrentDbStored.writeBytes(outputStream.toByteArray())

        dataStoreRepository.updateIsRestoreFromBackupWasSucceed(true)
    }
}