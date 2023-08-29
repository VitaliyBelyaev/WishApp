package ru.vitaliy.belyaev.wishapp.domain.repository

import android.content.Context
import java.io.File
import java.io.OutputStream
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo

interface BackupRepository {

    suspend fun checkExistingBackup(context: Context): BackupInfo

    suspend fun uploadNewBackup(
        context: Context,
        backupFile: File,
    ): BackupInfo.Value

    suspend fun downloadBackup(
        context: Context,
        fileId: String,
        outputStream: OutputStream,
    )
}