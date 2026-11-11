package ru.vitaliy.belyaev.wishapp.domain.use_case

import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository
import ru.vitaliy.belyaev.wishapp.domain.model.error.BackupFromNewerVersionException
import ru.vitaliy.belyaev.wishapp.domain.model.error.InvalidBackupFileException
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupLoadProgressListener
import ru.vitaliy.belyaev.wishapp.shared.data.database.BackupFileCheck
import ru.vitaliy.belyaev.wishapp.shared.data.database.BackupFileCheckResult

/**
 * Восстановление копии из облака. Копию, которую это приложение не поймёт, не ставим и текущие
 * данные не трогаем.
 *
 * Версию схемы копии узнаём без скачивания, если она известна: из свойств копии (пишутся с 1.9.1)
 * или из прошлого отказа той же копии. Запоминается версия схемы копии, а не сам отказ, и при
 * каждой попытке она сравнивается со схемой приложения: после обновления, где схема догнала копию,
 * копия скачивается и ставится как обычно.
 */
@Singleton
internal class RestoreBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val dataStoreRepository: DataStoreRepository
) {

    // Пока жив процесс: копия та же, пока совпадают id файла и время его изменения
    private val knownSchemaVersions = ConcurrentHashMap<String, Long>()

    suspend operator fun invoke(
        backup: BackupInfo.Value,
        fileWhereCurrentDbStored: File,
        progressListener: BackupLoadProgressListener,
    ) {
        val backupKey = "${backup.fileId}|${backup.modifiedDateTime}"
        val knownSchemaVersion = backup.schemaVersion ?: knownSchemaVersions[backupKey]
        if (knownSchemaVersion != null) {
            BackupFileCheck.checkSchemaVersion(knownSchemaVersion).throwIfNewer()
        }

        val outputStream = ByteArrayOutputStream()
        backupRepository.downloadBackup(
            fileId = backup.fileId,
            outputStream = outputStream,
            progressListener = progressListener
        )

        val backupBytes = outputStream.toByteArray()
        // Проверка до удаления текущей базы: копию, которую это приложение не поймёт, не ставим
        when (val result = BackupFileCheck.check(backupBytes)) {
            BackupFileCheckResult.Compatible -> Unit
            is BackupFileCheckResult.FromNewerVersion -> {
                knownSchemaVersions[backupKey] = result.backupSchemaVersion
                result.throwIfNewer()
            }
            BackupFileCheckResult.NotDatabase -> throw InvalidBackupFileException()
        }

        fileWhereCurrentDbStored.delete()
        fileWhereCurrentDbStored.writeBytes(backupBytes)

        dataStoreRepository.updateIsRestoreFromBackupWasSucceed(true)
    }

    private fun BackupFileCheckResult.throwIfNewer() {
        if (this is BackupFileCheckResult.FromNewerVersion) {
            throw BackupFromNewerVersionException(
                backupSchemaVersion = backupSchemaVersion,
                appSchemaVersion = appSchemaVersion,
            )
        }
    }
}
