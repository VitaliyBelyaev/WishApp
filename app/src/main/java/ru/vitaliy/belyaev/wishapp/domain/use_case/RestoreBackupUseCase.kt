package ru.vitaliy.belyaev.wishapp.domain.use_case

import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupLoadProgressListener
import ru.vitaliy.belyaev.wishapp.domain.model.error.BackupFromNewerVersionException
import ru.vitaliy.belyaev.wishapp.domain.model.error.InvalidBackupFileException
import ru.vitaliy.belyaev.wishapp.shared.data.database.BackupFileCheck
import ru.vitaliy.belyaev.wishapp.shared.data.database.BackupFileCheckResult

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

        val backupBytes = outputStream.toByteArray()
        // Проверка до удаления текущей базы: копию, которую это приложение не поймёт, не ставим
        when (val result = BackupFileCheck.check(backupBytes)) {
            BackupFileCheckResult.Compatible -> Unit
            is BackupFileCheckResult.FromNewerVersion -> throw BackupFromNewerVersionException(
                backupSchemaVersion = result.backupSchemaVersion,
                appSchemaVersion = result.appSchemaVersion,
            )
            BackupFileCheckResult.NotDatabase -> throw InvalidBackupFileException()
        }

        fileWhereCurrentDbStored.delete()
        fileWhereCurrentDbStored.writeBytes(backupBytes)

        dataStoreRepository.updateIsRestoreFromBackupWasSucceed(true)
    }
}