package ru.vitaliy.belyaev.wishapp.domain.use_case

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.domain.model.error.BackupFromNewerVersionException
import ru.vitaliy.belyaev.wishapp.domain.model.error.InvalidBackupFileException
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupFileVerifier
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupLoadProgressListener
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository
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
 *
 * Замена безопасная: копия скачивается во временный файл рядом с базой (`<база>.restore`),
 * проверяется (заголовок и версия схемы, потом открытие только на чтение и `PRAGMA quick_check`)
 * и встаёт на место базы одним переименованием в той же папке. Пока переименования не было, старая
 * база цела: сорвалось скачивание, кончилось место или копия битая — временный файл удаляется, и всё.
 * Открытое соединение после переименования держит прежний файл, его данные пропадут при перезапуске.
 * Журнал отката прежней базы удаляем, иначе при открытии SQLite откатил бы его в новую базу.
 */
@Singleton
internal class RestoreBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val backupFileVerifier: BackupFileVerifier,
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

        val restoreFile = File(fileWhereCurrentDbStored.path + RESTORE_SUFFIX)
        try {
            FileOutputStream(restoreFile).use { output ->
                backupRepository.downloadBackup(
                    fileId = backup.fileId,
                    outputStream = output,
                    progressListener = progressListener,
                )
                // Байты на диске до переименования: иначе сбой питания мог бы оставить пустую базу
                output.fd.sync()
            }

            checkRestoreFile(restoreFile, backupKey)

            // rename() в той же папке атомарен: на месте базы либо прежний файл, либо копия целиком
            if (!restoreFile.renameTo(fileWhereCurrentDbStored)) {
                throw IOException("Could not move restored backup into place")
            }
        } finally {
            restoreFile.delete()
        }
        JOURNAL_SUFFIXES.forEach { File(fileWhereCurrentDbStored.path + it).delete() }

        dataStoreRepository.updateIsRestoreFromBackupWasSucceed(true)
    }

    /** Проверка до замены текущей базы: копию, которую это приложение не поймёт, или битую не ставим. */
    private fun checkRestoreFile(restoreFile: File, backupKey: String) {
        val result = BackupFileCheck.check(restoreFile.readHeader())
        if (result is BackupFileCheckResult.FromNewerVersion) {
            knownSchemaVersions[backupKey] = result.backupSchemaVersion
            result.throwIfNewer()
        }
        if (result == BackupFileCheckResult.NotDatabase || !backupFileVerifier.isIntactDatabase(restoreFile)) {
            throw InvalidBackupFileException()
        }
    }

    private fun File.readHeader(): ByteArray = inputStream().use { it.readNBytesCompat(HEADER_BYTES) }

    private fun InputStream.readNBytesCompat(count: Int): ByteArray {
        val buffer = ByteArray(count)
        var read = 0
        while (read < count) {
            val n = read(buffer, read, count - read)
            if (n < 0) break
            read += n
        }
        return buffer.copyOf(read)
    }

    private fun BackupFileCheckResult.throwIfNewer() {
        if (this is BackupFileCheckResult.FromNewerVersion) {
            throw BackupFromNewerVersionException(
                backupSchemaVersion = backupSchemaVersion,
                appSchemaVersion = appSchemaVersion,
            )
        }
    }

    internal companion object {
        const val RESTORE_SUFFIX = ".restore"

        // Заголовок SQLite: сигнатура и версия схемы
        private const val HEADER_BYTES = 100
        private val JOURNAL_SUFFIXES = listOf("-journal", "-wal", "-shm")
    }
}
