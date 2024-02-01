package ru.vitaliy.belyaev.wishapp.domain.repository

import java.io.File
import java.io.OutputStream
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupLoadProgressListener

internal interface BackupRepository {

    suspend fun checkExistingBackup(): BackupInfo

    suspend fun uploadNewBackup(
        backupFile: File,
        progressListener: BackupLoadProgressListener,
    ): BackupInfo.Value

    suspend fun downloadBackup(
        fileId: String,
        outputStream: OutputStream,
        progressListener: BackupLoadProgressListener,
    )
}