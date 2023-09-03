package ru.vitaliy.belyaev.wishapp.domain.model

import java.time.LocalDateTime

sealed class BackupInfo {

    object None : BackupInfo()

    data class Value(
        val fileId: String,
        val modifiedDateTime: LocalDateTime,
        val sizeInBytes: Long,
        val accountEmail: String?,
        val device: String?,
    ) : BackupInfo()
}
