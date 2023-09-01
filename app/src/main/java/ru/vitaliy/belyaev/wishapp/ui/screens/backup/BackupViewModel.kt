package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import android.content.Context
import android.content.Intent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.BackupAndRestoreScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupAuthRepository
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository
import ru.vitaliy.belyaev.wishapp.domain.use_case.CreateBackupUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.IsUserSignedInToBackupServiceUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.NeedToShowRestoreUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.RestoreBackupUseCase
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.utils.restartApp
import timber.log.Timber

@HiltViewModel
internal class BackupViewModel @Inject constructor(
    private val createBackupUseCase: CreateBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    private val isUserSignedInToBackupServiceUseCase: IsUserSignedInToBackupServiceUseCase,
    private val backupRepository: BackupRepository,
    private val backupAuthRepository: BackupAuthRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val needToShowRestoreUseCase: NeedToShowRestoreUseCase,
    private val wishAppSdk: WishAppSdk,
) : BaseViewModel() {

    private val _viewState: MutableStateFlow<BackupViewState> = MutableStateFlow(BackupViewState.None)
    val viewState: StateFlow<BackupViewState> = _viewState.asStateFlow()

    private val _loadingState: MutableStateFlow<LoadingState> = MutableStateFlow(LoadingState.None)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    val signInIntent = backupAuthRepository.getSignInIntent()

    init {
        launchSafe {
            _loadingState.value = LoadingState.Empty

            val isUserSignedIn = isUserSignedInToBackupServiceUseCase()
            if (isUserSignedIn) {
                checkExistingBackup(loadingState = LoadingState.Empty)
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
            // todo show error snack
        } else {
            launchSafe {
                val isUserSignedIn = backupAuthRepository.checkIsSignedInFromIntent(intent)
                if (isUserSignedIn) {
                    checkExistingBackup(loadingState = LoadingState.OverData)
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
        checkExistingBackup(loadingState = LoadingState.OverData)
    }

    fun onCreateBackupClicked(context: Context) {
        launchSafe {
            _loadingState.value = LoadingState.OverData
            runCatching {
                withContext(Dispatchers.IO) {
                    createBackupUseCase(context.getDatabasePath(wishAppSdk.databaseName))
                }
            }.onSuccess { backupInfo ->
                handleNewBackupInfo(backupInfo)
            }.onFailure {
                Timber.e(it, message = "Error while creating backup")
                _loadingState.value = LoadingState.None
                // todo show error snack
            }
        }
    }

    fun onRestoreBackupClicked(context: Context) {
        launchSafe {
            _loadingState.value = LoadingState.OverData
            runCatching {
                withContext(Dispatchers.IO) {
                    restoreBackupUseCase(
                        backupFileId = (viewState.value as BackupViewState.CurrentBackupWithRestore).backupInfo.fileId,
                        fileWhereCurrentDbStored = context.getDatabasePath(wishAppSdk.databaseName)
                    )
                }
            }.onSuccess { backupInfo ->
                _loadingState.value = LoadingState.None
                // todo show success snack
                context.restartApp()
            }.onFailure {
                Timber.e(it, message = "Error while restoring backup")
                _loadingState.value = LoadingState.None
                // todo show error snack
            }
        }
    }

    private fun checkExistingBackup(loadingState: LoadingState) {
        launchSafe {
            runCatching {
                _loadingState.value = loadingState
                withContext(Dispatchers.IO) {
                    backupRepository.checkExistingBackup()
                }
            }.onSuccess { backupInfo ->
                handleNewBackupInfo(backupInfo)
            }.onFailure {
                Timber.e(it, message = "Error while checking existing backup")
                _loadingState.value = LoadingState.None
                _viewState.value = BackupViewState.CheckBackupError
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
                        BackupViewState.CurrentBackupWithRestore(backupInfo)
                    } else {
                        BackupViewState.CurrentBackup(backupInfo)
                    }
                    _loadingState.value = LoadingState.None
                    _viewState.value = newViewState
                }
            }
        }
    }
}