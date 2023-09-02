package ru.vitaliy.belyaev.wishapp.data.repository.backup

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import java.io.File
import java.io.OutputStream
import java.time.Instant
import java.time.ZoneId
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.domain.model.GoogleSignInException
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository

typealias DriveFile = com.google.api.services.drive.model.File

internal class GoogleDriveBackupRepository(
    private val context: Context
) : BackupRepository {

    private val httpTransport = NetHttpTransport()
    private val jsonFactory = JacksonFactory.getDefaultInstance()

    override suspend fun checkExistingBackup(): BackupInfo {
        val account = checkAccount(context)
        val drive = createDriveService(context, account)

        val resultFiles: FileList = drive.Files().list()
            .setSpaces(FOLDER_NAME_FOR_PRIVATE_APP_STORAGE)
            .setQ("name = '${BACKUP_FILE_NAME}' and mimeType = '${BACKUP_FILE_MIME_TYPE}'")
            .setFields("nextPageToken, files($FIELDS_TO_INCLUDE_IN_RESPONSE_FOR_BACKUP_FILE)")
            .setPageSize(3)
            .execute()

        return resultFiles.files.firstOrNull()?.toBackupInfo() ?: BackupInfo.None
    }

    override suspend fun uploadNewBackup(
        backupFile: File,
    ): BackupInfo.Value {
        val account = checkAccount(context)
        val drive = createDriveService(context, account)

        val fileMetadata = DriveFile().apply {
            name = BACKUP_FILE_NAME
        }

        val mediaContent = FileContent(BACKUP_FILE_MIME_TYPE, backupFile)

        val backupInfo = checkExistingBackup()
        val driveFile: DriveFile = if (backupInfo is BackupInfo.Value) {
            drive.Files().update(backupInfo.fileId, fileMetadata, mediaContent)
                .setFields(FIELDS_TO_INCLUDE_IN_RESPONSE_FOR_BACKUP_FILE)
                .execute()
        } else {
            fileMetadata.parents = listOf(FOLDER_NAME_FOR_PRIVATE_APP_STORAGE)
            drive.Files().create(fileMetadata, mediaContent)
                .setFields(FIELDS_TO_INCLUDE_IN_RESPONSE_FOR_BACKUP_FILE)
                .execute()
        }

        return driveFile.toBackupInfo()
    }

    override suspend fun downloadBackup(
        fileId: String,
        outputStream: OutputStream
    ) {
        val account = checkAccount(context)
        val drive = createDriveService(context, account)

        drive.Files().get(fileId).executeMediaAndDownloadTo(outputStream)
    }

    private fun checkAccount(context: Context): GoogleSignInAccount {
        return GoogleSignIn.getLastSignedInAccount(context) ?: throw GoogleSignInException()
    }

    private fun createDriveService(context: Context, account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA)
        ).apply {
            selectedAccount = account.account
        }

        return Drive.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(context.getString(R.string.app_name))
            .build()
    }

    private fun DriveFile.toBackupInfo(): BackupInfo.Value {
        val createdLocalDateTime = Instant.ofEpochMilli(modifiedTime.value)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        return BackupInfo.Value(
            fileId = id,
            modifiedDateTime = createdLocalDateTime,
            sizeInBytes = getSize()
        )
    }

    companion object {

        private const val BACKUP_FILE_NAME = "wishapp.backup"
        private const val BACKUP_FILE_MIME_TYPE = "application/octet-stream"

        // This is special folder name in Google Drive that is used for storing app data, invisible to user
        private const val FOLDER_NAME_FOR_PRIVATE_APP_STORAGE = "appDataFolder"

        private const val FIELDS_TO_INCLUDE_IN_RESPONSE_FOR_BACKUP_FILE = "id, name, createdTime, modifiedTime, size"
    }
}