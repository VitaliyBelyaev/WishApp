package ru.vitaliy.belyaev.wishapp.domain.repository

import java.io.File
import java.io.OutputStream
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo

internal interface BackupRepository {

    suspend fun checkExistingBackup(): BackupInfo

    suspend fun uploadNewBackup(
        backupFile: File,
    ): BackupInfo.Value

    suspend fun downloadBackup(
        fileId: String,
        outputStream: OutputStream,
    )
}