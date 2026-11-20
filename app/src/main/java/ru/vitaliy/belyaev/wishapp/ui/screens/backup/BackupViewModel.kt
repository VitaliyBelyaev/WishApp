package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.CurrentBackupInfoHolder
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.BackupScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.BackupCreateBackupClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.BackupCreateBackupSucceedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.BackupForceUpdateAppDataClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.BackupForceUpdateAppDataSucceedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.BackupGiveDrivePermissionClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.BackupInfoIconClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.BackupRefreshInfoClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.BackupRestoreBackupClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.BackupRestoreBackupSucceedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.error.BackupFailureReason
import ru.vitaliy.belyaev.wishapp.domain.model.error.BackupFromNewerVersionException
import ru.vitaliy.belyaev.wishapp.domain.model.error.CheckBackupException
import ru.vitaliy.belyaev.wishapp.domain.model.error.RestoreBackupException
import ru.vitaliy.belyaev.wishapp.domain.model.error.UploadNewBackupException
import ru.vitaliy.belyaev.wishapp.domain.model.error.backupFailureReason
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupAuthRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DispatcherProvider
import timber.log.Timber

/**
 * Резервная копия нового вида (`ScreenBackup` в дизайн-системе, состояния `bk` в прототипе).
 * Логика копии общая с прежним видом: те же use case'ы, здесь только состояние экрана и тексты ошибок
 * по причине (`ScreenErrors`).
 */
@HiltViewModel
internal class BackupViewModel @Inject constructor(
    private val useCases: BackupUseCases,
    private val currentBackupInfoHolder: CurrentBackupInfoHolder,
    private val backupAuthRepository: BackupAuthRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val dispatchers: DispatcherProvider,
) : BaseViewModel() {

    private val _state = MutableStateFlow(BackupState())
    val state: StateFlow<BackupState> = _state.asStateFlow()

    private val _events = Channel<BackupEvent>(Channel.BUFFERED)
    val events: Flow<BackupEvent> = _events.receiveAsFlow()

    val signInIntent: Intent
        get() = backupAuthRepository.getSignInIntent()

    init {
        launchSafe {
            if (useCases.isSignedIn()) {
                checkBackup(forceRemote = false)
            } else {
                // Вышли не из приложения: прежний ответ мог быть про другой аккаунт
                currentBackupInfoHolder.clear()
                setStatus(BackupStatus.SignedOut)
            }
        }
    }

    fun trackScreenShow() = analyticsRepository.trackEvent(BackupScreenShowEvent)

    fun onAllowAccessClicked() = analyticsRepository.trackEvent(BackupGiveDrivePermissionClickedEvent)

    fun onInfoClicked() = analyticsRepository.trackEvent(BackupInfoIconClickedEvent)

    fun onSignInResult(result: ActivityResult) {
        // Отмена выбора аккаунта не ошибка, сообщения нет
        if (result.resultCode == Activity.RESULT_CANCELED) return
        val intent = result.data
        launchSafe {
            val isSignedIn = intent != null &&
                result.resultCode == Activity.RESULT_OK &&
                backupAuthRepository.checkIsSignedInFromIntent(intent)
            if (isSignedIn) {
                checkBackup(forceRemote = false)
            } else {
                record(IllegalStateException("Sign-in failed, resultCode:${result.resultCode}"))
                _events.send(BackupEvent.Message(R.string.err_google_sign_in))
            }
        }
    }

    fun onRefreshClicked() {
        analyticsRepository.trackEvent(BackupRefreshInfoClickedEvent)
        launchSafe { checkBackup(forceRemote = true) }
    }

    fun onCreateClicked() {
        if (_state.value.work != null) return
        analyticsRepository.trackEvent(BackupCreateBackupClickedEvent)
        launchSafe {
            _state.update { it.copy(work = BackupWork.Saving()) }
            runCatchingCancellable {
                withContext(dispatchers.io()) {
                    useCases.createBackup(useCases.databaseFile) { fraction, loaded ->
                        _state.update { it.copy(work = BackupWork.Saving(progress(fraction, loaded))) }
                    }
                }
            }.onSuccess { backup ->
                analyticsRepository.trackEvent(BackupCreateBackupSucceedEvent)
                _state.update { it.copy(work = null) }
                showBackup(backup)
                _events.send(BackupEvent.Message(R.string.snack_backup_created))
            }.onFailure { error ->
                record(UploadNewBackupException(error))
                _state.update { it.copy(work = null) }
                onFailure(
                    reason = error.backupFailureReason(),
                    noInternet = R.string.err_backup_create_net,
                    other = R.string.err_backup_create,
                    retry = ::onCreateClicked,
                )
            }
        }
    }

    /** Подтвердили «Вернуть данные» или «Заменить данные копией». */
    fun onRestoreConfirmed() {
        val status = _state.value.status as? BackupStatus.HasBackup ?: return
        if (_state.value.work != null) return
        val restoreEvent = if (status.isRestoreSuggested) {
            BackupRestoreBackupClickedEvent
        } else {
            BackupForceUpdateAppDataClickedEvent
        }
        analyticsRepository.trackEvent(restoreEvent)
        launchSafe {
            _state.update { it.copy(work = BackupWork.Restoring()) }
            runCatchingCancellable {
                withContext(dispatchers.io()) {
                    useCases.restoreBackup(status.backup, useCases.databaseFile) { fraction, loaded ->
                        _state.update { it.copy(work = BackupWork.Restoring(progress(fraction, loaded))) }
                    }
                }
            }.onSuccess {
                val succeedEvent = if (status.isRestoreSuggested) {
                    BackupRestoreBackupSucceedEvent
                } else {
                    BackupForceUpdateAppDataSucceedEvent
                }
                analyticsRepository.trackEvent(succeedEvent)
                // Экран остаётся заблокированным до перезапуска: данные уже заменены
                _events.send(BackupEvent.Restored)
            }.onFailure { error ->
                _state.update { it.copy(work = null) }
                // Не сбой, а ожидаемый случай: данные не тронуты, нужна новая версия приложения
                if (error is BackupFromNewerVersionException) {
                    Timber.w(error)
                    _events.send(BackupEvent.NewerVersion)
                    return@onFailure
                }
                record(RestoreBackupException(error))
                onFailure(
                    reason = error.backupFailureReason(),
                    noInternet = R.string.err_backup_restore_net,
                    other = R.string.err_backup_restore,
                    retry = ::onRestoreConfirmed,
                )
            }
        }
    }

    fun onSignOutClicked() = leaveAccount { backupAuthRepository.signOut() }

    fun onDisconnectClicked() = leaveAccount { backupAuthRepository.disconnectAccount() }

    private fun leaveAccount(action: suspend () -> Unit) {
        if (_state.value.work != null) return
        launchSafe {
            runCatchingCancellable { withContext(dispatchers.io()) { action() } }
                .onSuccess {
                    currentBackupInfoHolder.clear()
                    setStatus(BackupStatus.SignedOut)
                }
                .onFailure { record(IllegalStateException("Error while leaving backup account", it)) }
        }
    }

    private suspend fun checkBackup(forceRemote: Boolean) {
        // Копию уже проверяли в этом запуске: показываем сразу, без «Проверяем», обновить — кнопкой
        if (!forceRemote && currentBackupInfoHolder.isKnown) {
            showInfo(currentBackupInfoHolder.backupInfo.value)
            return
        }
        val previous = _state.value.status
        setStatus(BackupStatus.Checking(previous.account ?: backupAuthRepository.signedInEmail()))
        val checkingShown = TimeSource.Monotonic.markNow()
        val result = runCatchingCancellable {
            withContext(dispatchers.io()) { useCases.getExistingBackupInfo(forceRemote) }
        }
        // «Проверяем копию» держится хотя бы полсекунды: мгновенная ошибка без сети не мелькает карточкой
        delay(MIN_CHECKING_TIME - checkingShown.elapsedNow())
        result.onSuccess { info -> showInfo(info) }.onFailure { error ->
            record(CheckBackupException(error))
            val reason = error.backupFailureReason()
            if (reason == BackupFailureReason.NO_ACCESS) {
                currentBackupInfoHolder.clear()
                setStatus(BackupStatus.SignedOut)
                _events.send(BackupEvent.Message(R.string.err_drive_revoked))
                return@onFailure
            }
            // Копию уже показывали: оставляем её и говорим об ошибке снэкбаром, как раньше
            if (previous is BackupStatus.HasBackup) {
                setStatus(previous)
                val text = if (reason == BackupFailureReason.NO_INTERNET) {
                    R.string.err_backup_check_net
                } else {
                    R.string.err_backup_check
                }
                _events.send(BackupEvent.Message(text, retry = ::onRefreshClicked))
            } else {
                setStatus(BackupStatus.CheckFailed(previous.account ?: backupAuthRepository.signedInEmail()))
            }
        }
    }

    private suspend fun showInfo(info: BackupInfo) = when (info) {
        is BackupInfo.None -> setStatus(
            BackupStatus.NoBackup(info.accountEmail ?: backupAuthRepository.signedInEmail()),
        )
        is BackupInfo.Value -> showBackup(info)
    }

    private suspend fun showBackup(backup: BackupInfo.Value) {
        currentBackupInfoHolder.updateBackupInfo(backup)
        setStatus(BackupStatus.HasBackup(backup, isRestoreSuggested = useCases.needToShowRestore()))
    }

    private suspend fun onFailure(reason: BackupFailureReason, noInternet: Int, other: Int, retry: () -> Unit) {
        when (reason) {
            BackupFailureReason.NO_ACCESS -> {
                currentBackupInfoHolder.clear()
                setStatus(BackupStatus.SignedOut)
                _events.send(BackupEvent.Message(R.string.err_drive_revoked))
            }
            BackupFailureReason.NO_INTERNET -> _events.send(BackupEvent.Message(noInternet, retry))
            BackupFailureReason.DRIVE_FULL -> _events.send(BackupEvent.Message(R.string.err_backup_drive_full, retry))
            BackupFailureReason.OTHER -> _events.send(BackupEvent.Message(other, retry))
        }
    }

    private fun setStatus(status: BackupStatus) = _state.update {
        val hasAccount = status.account != null
        it.copy(
            status = status,
            accountPhotoUrl = backupAuthRepository.signedInPhotoUrl().takeIf { hasAccount },
            accountName = backupAuthRepository.signedInName().takeIf { hasAccount },
        )
    }

    private fun record(error: Throwable) {
        Timber.e(error)
        FirebaseCrashlytics.getInstance().recordException(error)
    }

    private fun progress(fraction: Double, loadedBytes: Long): BackupProgress? {
        if (fraction <= 0.0) return null
        return BackupProgress(
            fraction = fraction.toFloat().coerceIn(0f, 1f),
            loadedBytes = loadedBytes,
            totalBytes = (loadedBytes / fraction).toLong(),
        )
    }
}

private val MIN_CHECKING_TIME = 500.milliseconds

/**
 * `runCatching` без отмены: ушли с экрана посреди сохранения — это не ошибка копии, её не пишем
 * в Crashlytics и не показываем, а отмену пробрасываем дальше.
 */
private inline fun <T> runCatchingCancellable(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    Result.failure(error)
}
