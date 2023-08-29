package ru.vitaliy.belyaev.wishapp.domain.model

import java.time.LocalDateTime

sealed class BackupInfo {

    object None : BackupInfo()

    data class Value(
        val fileId: String,
        val createdDateTime: LocalDateTime,
        val sizeInBytes: Long,
    ) : BackupInfo()
}
