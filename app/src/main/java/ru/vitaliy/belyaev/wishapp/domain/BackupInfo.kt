package ru.vitaliy.belyaev.wishapp.domain

import java.time.LocalDateTime

sealed class BackupInfo {

    object None : BackupInfo()

    data class Value(
        val driveFileId: String,
        val createdDateTime: LocalDateTime,
        val sizeInBytes: Long,
    ) : BackupInfo()
}
