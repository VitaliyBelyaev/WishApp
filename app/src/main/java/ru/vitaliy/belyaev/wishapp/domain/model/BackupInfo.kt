package ru.vitaliy.belyaev.wishapp.domain.model

import java.time.LocalDateTime

sealed class BackupInfo {

    abstract val accountEmail: String?

    data class None(override val accountEmail: String? = null) : BackupInfo()

    data class Value(
        val fileId: String,
        val modifiedDateTime: LocalDateTime,
        val sizeInBytes: Long,
        override val accountEmail: String?,
        val device: String?,
        /** Версия схемы базы в копии. Пишется с 1.9.1, у копий из прошлых версий её нет. */
        val schemaVersion: Long? = null,
    ) : BackupInfo()
}
