package ru.vitaliy.belyaev.wishapp.domain.model.error

import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Почему не получилось действие с копией: от причины зависит текст ошибки и что предложить. */
enum class BackupFailureReason {
    /** Нет сети или сервер не ответил. */
    NO_INTERNET,

    /** В Google Drive закончилось место, копия не загрузилась. */
    DRIVE_FULL,

    /** Не вошли в аккаунт или доступ к Drive отозван: нужен вход заново. */
    NO_ACCESS,
    OTHER,
}

/** Причина по цепочке `cause`: ошибки копии обёрнуты, исходная лежит глубже. */
fun Throwable.backupFailureReason(): BackupFailureReason {
    val chain = generateSequence(this) { it.cause }.take(MAX_CAUSE_DEPTH).toList()
    return when {
        chain.any { it.isNoAccess() } -> BackupFailureReason.NO_ACCESS
        chain.any { it.isDriveFull() } -> BackupFailureReason.DRIVE_FULL
        chain.any { it.isNoInternet() } -> BackupFailureReason.NO_INTERNET
        else -> BackupFailureReason.OTHER
    }
}

private fun Throwable.isNoAccess(): Boolean =
    this is UserRecoverableAuthIOException || this is UserRecoverableAuthException || this is GoogleSignInException

// Drive отвечает 403 с причиной storageQuotaExceeded, когда место в аккаунте кончилось
private fun Throwable.isDriveFull(): Boolean =
    this is GoogleJsonResponseException && details?.errors.orEmpty().any { it.reason == DRIVE_FULL_REASON }

private fun Throwable.isNoInternet(): Boolean =
    this is UnknownHostException ||
        this is ConnectException ||
        this is NoRouteToHostException ||
        this is SocketTimeoutException

private const val DRIVE_FULL_REASON = "storageQuotaExceeded"
private const val MAX_CAUSE_DEPTH = 8
