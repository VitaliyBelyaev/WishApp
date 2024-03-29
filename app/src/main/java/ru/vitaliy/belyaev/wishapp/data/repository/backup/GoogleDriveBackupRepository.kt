package ru.vitaliy.belyaev.wishapp.data.repository.backup

import android.content.Context
import android.os.Build
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.media.MediaHttpDownloader
import com.google.api.client.googleapis.media.MediaHttpUploader
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
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.domain.model.error.GoogleSignInException
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupLoadProgressListener

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

        return resultFiles.files.firstOrNull()?.toBackupInfo(account) ?: BackupInfo.None(account.email)
    }

    override suspend fun uploadNewBackup(
        backupFile: File,
        progressListener: BackupLoadProgressListener,
    ): BackupInfo.Value {
        val account = checkAccount(context)
        val drive = createDriveService(context, account)

        val fileMetadata = DriveFile().apply {
            name = BACKUP_FILE_NAME
            appProperties = createAppProperties()
        }

        val mediaContent = FileContent(BACKUP_FILE_MIME_TYPE, backupFile)

        val backupInfo = checkExistingBackup()

        val uploadRequest = if (backupInfo is BackupInfo.Value) {
            drive.Files().update(backupInfo.fileId, fileMetadata, mediaContent)
        } else {
            fileMetadata.parents = listOf(FOLDER_NAME_FOR_PRIVATE_APP_STORAGE)
            drive.Files().create(fileMetadata, mediaContent)
        }

        val driveFile: DriveFile = uploadRequest
            .setFields(FIELDS_TO_INCLUDE_IN_RESPONSE_FOR_BACKUP_FILE)
            .apply { configureUploader(mediaHttpUploader, progressListener) }
            .execute()

        return driveFile.toBackupInfo(account)
    }

    private fun configureUploader(
        uploader: MediaHttpUploader,
        progressListener: BackupLoadProgressListener
    ) {
        uploader.isDirectUploadEnabled = false
        uploader.chunkSize = LOAD_CHUNK_SIZE
        uploader.setProgressListener {
            if (it.progress == 0.0) {
                return@setProgressListener
            }
            progressListener.onProgressChanged(it.progress, it.numBytesUploaded)
        }
    }

    private fun configureDownloader(
        downloader: MediaHttpDownloader,
        progressListener: BackupLoadProgressListener
    ) {
        downloader.isDirectDownloadEnabled = false
        downloader.chunkSize = LOAD_CHUNK_SIZE
        downloader.setProgressListener {
            if (it.progress == 0.0) {
                return@setProgressListener
            }
            progressListener.onProgressChanged(it.progress, it.numBytesDownloaded)
        }
    }

    override suspend fun downloadBackup(
        fileId: String,
        outputStream: OutputStream,
        progressListener: BackupLoadProgressListener,
    ) {
        val account = checkAccount(context)
        val drive = createDriveService(context, account)

        drive.Files()
            .get(fileId)
            .apply { configureDownloader(mediaHttpDownloader, progressListener) }
            .executeMediaAndDownloadTo(outputStream)
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

    private fun DriveFile.toBackupInfo(account: GoogleSignInAccount): BackupInfo.Value {
        val createdLocalDateTime = Instant.ofEpochMilli(modifiedTime.value)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        return BackupInfo.Value(
            fileId = id,
            modifiedDateTime = createdLocalDateTime,
            sizeInBytes = getSize(),
            accountEmail = account.email,
            device = appProperties.extractDevice(),
        )
    }

    private fun createAppProperties(): Map<String, String> {
        return mapOf(
            KEY_APP_VERSION_NAME to BuildConfig.VERSION_NAME,
            KEY_APP_VERSION_NUMBER to BuildConfig.VERSION_CODE.toString(),
            KEY_DEVICE to "${Build.MANUFACTURER} ${Build.MODEL}",
        )
    }

    private fun Map<String, String>?.extractDevice(): String? {
        return this?.get(KEY_DEVICE)
    }

    companion object {

        private const val BACKUP_FILE_NAME = "wishapp.backup"
        private const val BACKUP_FILE_MIME_TYPE = "application/octet-stream"

        // This is special folder name in Google Drive that is used for storing app data, invisible to user
        private const val FOLDER_NAME_FOR_PRIVATE_APP_STORAGE = "appDataFolder"

        private const val FIELDS_TO_INCLUDE_IN_RESPONSE_FOR_BACKUP_FILE =
            "id, name, createdTime, modifiedTime, size, appProperties"

        private const val KEY_DEVICE = "device"
        private const val KEY_APP_VERSION_NAME = "appVersionName"
        private const val KEY_APP_VERSION_NUMBER = "appVersionNumber"

        private const val LOAD_CHUNK_SIZE = MediaHttpUploader.MINIMUM_CHUNK_SIZE * 2 // 512 KB
    }
}