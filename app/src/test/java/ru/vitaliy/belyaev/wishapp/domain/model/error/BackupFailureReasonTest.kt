package ru.vitaliy.belyaev.wishapp.domain.model.error

import com.google.api.client.googleapis.json.GoogleJsonError
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.HttpResponseException
import java.io.IOException
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupFailureReasonTest {

    @Test
    fun findsNoInternetInsideWrapper() {
        assertEquals(
            BackupFailureReason.NO_INTERNET,
            UploadNewBackupException(UnknownHostException("www.googleapis.com")).backupFailureReason(),
        )
    }

    @Test
    fun findsFullDrive() {
        val error = GoogleJsonError().apply {
            code = 403
            errors = listOf(GoogleJsonError.ErrorInfo().apply { reason = "storageQuotaExceeded" })
        }
        val response =
            GoogleJsonResponseException(HttpResponseException.Builder(403, "Forbidden", HttpHeaders()), error)

        assertEquals(BackupFailureReason.DRIVE_FULL, UploadNewBackupException(response).backupFailureReason())
    }

    @Test
    fun findsLostAccess() {
        assertEquals(BackupFailureReason.NO_ACCESS, CheckBackupException(GoogleSignInException()).backupFailureReason())
    }

    @Test
    fun otherErrorsStayOther() {
        assertEquals(BackupFailureReason.OTHER, RestoreBackupException(IOException("disk")).backupFailureReason())
    }
}
