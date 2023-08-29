package ru.vitaliy.belyaev.wishapp.domain.repository

import java.io.File
import java.io.OutputStream
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo

class BackupRepository {

    suspend fun checkExistingBackup(): BackupInfo {
        return BackupInfo.None
    }

    suspend fun uploadNewBackup(
        backupFile: File,
        nameWithExtension: String
    ): BackupInfo.Value {
        return BackupInfo.Value(
            fileId = "",
            createdDateTime = java.time.LocalDateTime.now(),
            sizeInBytes = 0L,
        )
    }

    suspend fun downloadBackup(
        fileId: String,
        outputStream: OutputStream,
    ) {
    }
}