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