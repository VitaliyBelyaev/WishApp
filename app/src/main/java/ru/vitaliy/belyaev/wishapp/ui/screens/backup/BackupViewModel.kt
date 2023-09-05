package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import android.content.Context
import android.content.Intent
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.BackupAndRestoreScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.model.error.CheckBackupException
import ru.vitaliy.belyaev.wishapp.domain.model.error.RestoreBackupException
import ru.vitaliy.belyaev.wishapp.domain.model.error.UploadNewBackupException
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupAuthRepository
import ru.vitaliy.belyaev.wishapp.domain.use_case.CreateBackupUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.GetExistingBackupInfoUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.IsUserSignedInToBackupServiceUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.NeedToShowRestoreUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.RestoreBackupUseCase
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.ui.core.snackbar.SnackbarMessage
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.utils.restartAppWithDelayMillis
import timber.log.Timber

@HiltViewModel
internal class BackupViewModel @Inject constructor(
    private val createBackupUseCase: CreateBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    private val isUserSignedInToBackupServiceUseCase: IsUserSignedInToBackupServiceUseCase,
    private val getExistingBackupInfoUseCase: GetExistingBackupInfoUseCase,
    private val backupAuthRepository: BackupAuthRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val needToShowRestoreUseCase: NeedToShowRestoreUseCase,
    private val wishAppSdk: WishAppSdk,
) : BaseViewModel() {

    private val _viewState: MutableStateFlow<BackupViewState> = MutableStateFlow(BackupViewState.None)
    val viewState: StateFlow<BackupViewState> = _viewState.asStateFlow()

    private val _loadingState: MutableStateFlow<LoadingState> = MutableStateFlow(LoadingState.None)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    private val _showSnackFlow = MutableSharedFlow<SnackbarMessage>()
    val showSnackFlow: SharedFlow<SnackbarMessage> = _showSnackFlow.asSharedFlow()

    val signInIntent = backupAuthRepository.getSignInIntent()

    init {
        launchSafe {
            _loadingState.value = LoadingState.Empty

            val isUserSignedIn = isUserSignedInToBackupServiceUseCase()
            if (isUserSignedIn) {
                checkExistingBackup(forceRemote = false)
            } else {
                _loadingState.value = LoadingState.None
                _viewState.value = BackupViewState.DrivePermissionRationale
            }
        }
    }

    fun trackScreenShow() {
        analyticsRepository.trackEvent(BackupAndRestoreScreenShowEvent)
    }

    fun onSignInResultReceived(intent: Intent?) {
        if (intent == null) {
            _viewState.value = BackupViewState.DrivePermissionRationale
            launchSafe {
                _showSnackFlow.emit(SnackbarMessage.StringValue.Message("Произошла ошибка при входе через Google аккаунт"))
            }
        } else {
            launchSafe {
                val isUserSignedIn = backupAuthRepository.checkIsSignedInFromIntent(intent)
                if (isUserSignedIn) {
                    checkExistingBackup(forceRemote = false)
                } else {
                    _loadingState.value = LoadingState.None
                    _viewState.value = BackupViewState.DrivePermissionRationale
                }
            }
        }
    }

    fun onGiveDrivePermissionClicked() {
    }

    fun onRetryCheckBackupClicked() {
        checkExistingBackup(forceRemote = true)
    }

    fun onCreateBackupClicked(context: Context) {
        launchSafe {
            _loadingState.value = LoadingState.UploadingNewBackup
            runCatching {
                withContext(Dispatchers.IO) {
                    createBackupUseCase(context.getDatabasePath(wishAppSdk.databaseName))
                }
            }.onSuccess { backupInfo ->
                handleNewBackupInfo(backupInfo)
                _showSnackFlow.emit(SnackbarMessage.StringValue.Message("Резервная копия успешно создана"))
            }.onFailure {
                val error = UploadNewBackupException(it)
                Timber.e(error)
                FirebaseCrashlytics.getInstance().recordException(error)

                _loadingState.value = LoadingState.None

                if (it is UserRecoverableAuthIOException) {
                    _viewState.value = BackupViewState.DrivePermissionRationale
                    return@launchSafe
                }
                _showSnackFlow.emit(SnackbarMessage.StringValue.Message("Произошла ошибка при создании резервной копии"))
            }
        }
    }

    fun onRefreshBackupInfoClicked() {
        checkExistingBackup(forceRemote = true)
    }

    fun onRestoreBackupClicked(context: Context) {
        launchSafe {
            _loadingState.value = LoadingState.RestoringBackup
            runCatching {
                withContext(Dispatchers.IO) {
                    restoreBackupUseCase(
                        backupFileId = (viewState.value as BackupViewState.CurrentBackup).backupInfo.fileId,
                        fileWhereCurrentDbStored = context.getDatabasePath(wishAppSdk.databaseName)
                    )
                }
            }.onSuccess {
                _showSnackFlow.emit(SnackbarMessage.StringValue.Message("Данные успешно восстановлены, сейчас приложение перезапустится"))
                context.restartAppWithDelayMillis(3000)
            }.onFailure {
                val error = RestoreBackupException(it)
                Timber.e(error)
                FirebaseCrashlytics.getInstance().recordException(error)

                _loadingState.value = LoadingState.None
                if (it is UserRecoverableAuthIOException) {
                    _viewState.value = BackupViewState.DrivePermissionRationale
                    return@launchSafe
                }

                _showSnackFlow.emit(SnackbarMessage.StringValue.Message("Произошла ошибка при восстановлении данных"))
            }
        }
    }

    private fun checkExistingBackup(forceRemote: Boolean) {
        launchSafe {
            runCatching {
                _loadingState.value = LoadingState.CheckingBackup
                withContext(Dispatchers.IO) { getExistingBackupInfoUseCase(forceRemote) }
            }.onSuccess { backupInfo ->
                handleNewBackupInfo(backupInfo)
            }.onFailure {
                val error = CheckBackupException(it)
                Timber.e(error)
                FirebaseCrashlytics.getInstance().recordException(error)

                _loadingState.value = LoadingState.None

                if (it is UserRecoverableAuthIOException) {
                    _viewState.value = BackupViewState.DrivePermissionRationale
                    return@launchSafe
                }
                when (viewState.value) {
                    is BackupViewState.CurrentBackup -> {
                        _showSnackFlow.emit(SnackbarMessage.StringValue.Message("Произошла ошибка при проверке наличия резервной копии"))
                    }

                    else -> {
                        _viewState.value = BackupViewState.CheckBackupError
                    }
                }
            }
        }
    }

    private fun handleNewBackupInfo(backupInfo: BackupInfo) {
        when (backupInfo) {
            is BackupInfo.None -> {
                _loadingState.value = LoadingState.None
                _viewState.value = BackupViewState.NoBackup
            }

            is BackupInfo.Value -> {
                launchSafe {
                    val newViewState = if (needToShowRestoreUseCase()) {
                        BackupViewState.CurrentBackup.WithRestore(backupInfo)
                    } else {
                        BackupViewState.CurrentBackup.WithForceUpdate(backupInfo)
                    }
                    _loadingState.value = LoadingState.None
                    _viewState.value = newViewState
                }
            }
        }
    }
}