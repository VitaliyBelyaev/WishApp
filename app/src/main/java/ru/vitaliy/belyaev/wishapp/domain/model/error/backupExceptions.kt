package ru.vitaliy.belyaev.wishapp.domain.model.error

class CheckBackupException(override val cause: Throwable? = null) : RuntimeException() {

    override val message: String = "Error while checking backup"
}

class RestoreBackupException(override val cause: Throwable? = null) : RuntimeException() {

    override val message: String = "Error while restoring backup"
}

class UploadNewBackupException(override val cause: Throwable? = null) : RuntimeException() {

    override val message: String = "Error while uploading new backup"
}

/** Копия сделана в версии приложения с более новой схемой базы. Данные не тронуты, нужно обновиться. */
class BackupFromNewerVersionException(
    val backupSchemaVersion: Long,
    val appSchemaVersion: Long,
) : RuntimeException() {

    override val message: String =
        "Backup schema version $backupSchemaVersion is newer than app schema version $appSchemaVersion"
}

/** Скачанный файл не база SQLite. Данные не тронуты. */
class InvalidBackupFileException : RuntimeException() {

    override val message: String = "Downloaded backup is not an SQLite database"
}
