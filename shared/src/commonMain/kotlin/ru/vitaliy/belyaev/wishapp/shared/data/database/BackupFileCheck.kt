package ru.vitaliy.belyaev.wishapp.shared.data.database

/** Можно ли восстановить скачанную копию базы в этой версии приложения. */
sealed interface BackupFileCheckResult {

    data object Compatible : BackupFileCheckResult

    /**
     * Копия сделана в версии приложения с более новой схемой. Старое приложение её не поймёт:
     * миграции назад нет, поэтому текущие данные не трогаем и просим обновиться.
     */
    data class FromNewerVersion(val backupSchemaVersion: Long, val appSchemaVersion: Long) : BackupFileCheckResult

    /** Файл не база SQLite: обрезан при скачивании или это не копия. */
    data object NotDatabase : BackupFileCheckResult
}

/**
 * Проверка копии до замены базы. Читает только заголовок SQLite-файла, базу не открывает:
 * `PRAGMA user_version`, в котором драйвер SQLDelight хранит версию схемы, лежит в заголовке
 * по смещению 60, четыре байта big-endian.
 */
object BackupFileCheck {

    private val SqliteMagic: ByteArray = "SQLite format 3\u0000".encodeToByteArray()
    private const val HeaderSize = 100
    private const val UserVersionOffset = 60

    fun check(
        fileHeader: ByteArray,
        appSchemaVersion: Long = WishAppDb.Schema.version,
    ): BackupFileCheckResult {
        if (fileHeader.size < HeaderSize) return BackupFileCheckResult.NotDatabase
        if (!SqliteMagic.indices.all { fileHeader[it] == SqliteMagic[it] }) return BackupFileCheckResult.NotDatabase

        val backupSchemaVersion = readUserVersion(fileHeader)
        return if (backupSchemaVersion > appSchemaVersion) {
            BackupFileCheckResult.FromNewerVersion(backupSchemaVersion, appSchemaVersion)
        } else {
            BackupFileCheckResult.Compatible
        }
    }

    private fun readUserVersion(header: ByteArray): Long =
        (0 until 4).fold(0L) { acc, i -> (acc shl 8) or (header[UserVersionOffset + i].toLong() and 0xFF) }
}
