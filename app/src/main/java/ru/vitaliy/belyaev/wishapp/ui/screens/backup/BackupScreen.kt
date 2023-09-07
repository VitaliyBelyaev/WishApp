package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.BackupScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.BackupForceUpdateAppDataClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.BackupGiveDrivePermissionClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.BackupInfoIconClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.BackupRestoreBackupClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.core.alert_dialog.DestructiveConfirmationAlertDialog
import ru.vitaliy.belyaev.wishapp.ui.core.bottomsheet.WishAppBottomSheetM3
import ru.vitaliy.belyaev.wishapp.ui.core.loader.FullscreenLoaderWithText
import ru.vitaliy.belyaev.wishapp.ui.core.snackbar.SnackbarMessage
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.components.BackupSheetContent
import ru.vitaliy.belyaev.wishapp.ui.theme.CommonColors
import ru.vitaliy.belyaev.wishapp.utils.BytesSizeFormatter
import ru.vitaliy.belyaev.wishapp.utils.getDataOrNull
import ru.vitaliy.belyaev.wishapp.utils.showDismissableSnackbar
import ru.vitaliy.belyaev.wishapp.utils.trackScreenShow

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
internal fun BackupScreen(
    onBackPressed: () -> Unit,
    analyticsRepository: AnalyticsRepository,
    viewModel: BackupViewModel = hiltViewModel(),
    appViewModel: AppActivityViewModel = hiltViewModel(LocalContext.current as AppActivity),
) {

    val viewState by viewModel.viewState.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val systemUiController = rememberSystemUiController()
    val scrollState: ScrollState = rememberScrollState()

    val openCreateBackupConfirmationDialog: MutableState<Boolean> = remember { mutableStateOf(false) }
    val openRestoreBackupConfirmationDialog: MutableState<Boolean> = remember { mutableStateOf(false) }
    val openForceUpdateAppDataConfirmationDialog: MutableState<Boolean> = remember { mutableStateOf(false) }

    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    trackScreenShow { analyticsRepository.trackEvent(BackupScreenShowEvent) }

    systemUiController.setNavigationBarColor(color = CommonColors.navBarColor())

    val drivePermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.onSignInResultReceived(result.getDataOrNull())
        }

    LaunchedEffect(key1 = Unit) {
        viewModel.showSnackFlow.collect {
            val message: String = when (it) {
                is SnackbarMessage.StringResInt -> context.getString(it.value)
                is SnackbarMessage.StringValue -> it.value
            }
            snackbarHostState.showDismissableSnackbar(message)
        }
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            WishAppTopBar(
                stringResource(R.string.backup),
                withBackIcon = true,
                onBackPressed = onBackPressed,
                scrollBehavior = topAppBarScrollBehavior,
                actions = {
                    IconButton(
                        onClick = {
                            analyticsRepository.trackEvent(BackupInfoIconClickedEvent)
                            showBottomSheet = true
                        }
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Backup info"
                        )
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        actionOnNewLine = true,
                    )
                }
            )
        }
    ) { pd ->
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(pd)
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
            )

            when (val state = viewState) {
                is BackupViewState.None -> {
                }
                is BackupViewState.DrivePermissionRationale -> {
                    DrivePermissionRationaleView {
                        analyticsRepository.trackEvent(BackupGiveDrivePermissionClickedEvent)
                        drivePermissionLauncher.launch(viewModel.signInIntent)
                    }
                }
                is BackupViewState.NoBackup -> {
                    CurrentBackupView(
                        onCreateBackupClicked = { viewModel.onCreateBackupClicked(context) },
                        onRefreshBackupInfoClicked = { viewModel.onRefreshBackupInfoClicked() }
                    )
                }
                is BackupViewState.CheckBackupError -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.backup_check_backup_error_message),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(onClick = { viewModel.onRetryCheckBackupClicked() }) {
                            Text(text = stringResource(R.string.backup_retry_check))
                        }
                    }
                }
                is BackupViewState.CurrentBackup -> {
                    CurrentBackupView(
                        backupInfo = state.backupInfo,
                        onCreateBackupClicked = {
                            when (state) {
                                is BackupViewState.CurrentBackup.WithRestore -> {
                                    openCreateBackupConfirmationDialog.value = true
                                }

                                is BackupViewState.CurrentBackup.WithForceUpdate -> {
                                    viewModel.onCreateBackupClicked(context)
                                }
                            }
                        },
                        onRefreshBackupInfoClicked = { viewModel.onRefreshBackupInfoClicked() }
                    )
                    Divider(modifier = Modifier.padding(16.dp))

                    when (state) {
                        is BackupViewState.CurrentBackup.WithRestore -> {
                            RestoreBackupView(
                                onRestoreBackupClicked = {
                                    analyticsRepository.trackEvent(BackupRestoreBackupClickedEvent)
                                    openRestoreBackupConfirmationDialog.value = true
                                }
                            )
                        }

                        is BackupViewState.CurrentBackup.WithForceUpdate -> {
                            ForceUpdateAppDataView(
                                onForceUpdateClicked = {
                                    analyticsRepository.trackEvent(BackupForceUpdateAppDataClickedEvent)
                                    openForceUpdateAppDataConfirmationDialog.value = true
                                }
                            )
                        }
                    }
                }
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
            )
        }

        if (openCreateBackupConfirmationDialog.value) {
            DestructiveConfirmationAlertDialog(
                onDismissRequest = { openCreateBackupConfirmationDialog.value = false },
                title = { Text(stringResource(R.string.backup_create_backup_confirm_dialog_title)) },
                text = { Text(stringResource(R.string.backup_create_backup_confirm_dialog_message)) },
                confirmButtonText = { Text(stringResource(R.string.backup_create_backup_confirm_dialog_positive_text)) },
                confirmClick = {
                    openCreateBackupConfirmationDialog.value = false
                    viewModel.onCreateBackupClicked(context)
                },
            )
        }

        if (openRestoreBackupConfirmationDialog.value) {
            DestructiveConfirmationAlertDialog(
                onDismissRequest = { openRestoreBackupConfirmationDialog.value = false },
                title = { Text(stringResource(R.string.backup_restore_backup_confirm_dialog_title)) },
                text = { Text(stringResource(R.string.backup_restore_backup_confirm_dialog_message)) },
                confirmButtonText = { Text(stringResource(R.string.backup_restore_backup_confirm_dialog_positive_text)) },
                confirmClick = {
                    openRestoreBackupConfirmationDialog.value = false
                    viewModel.onRestoreBackupClicked(context)
                },
            )
        }

        if (openForceUpdateAppDataConfirmationDialog.value) {
            DestructiveConfirmationAlertDialog(
                onDismissRequest = { openForceUpdateAppDataConfirmationDialog.value = false },
                title = { Text(stringResource(R.string.backup_force_update_confirm_dialog_title)) },
                text = { Text(stringResource(R.string.backup_force_update_confirm_dialog_message)) },
                confirmButtonText = { Text(stringResource(R.string.backup_force_update_confirm_dialog_positive_text)) },
                confirmClick = {
                    openForceUpdateAppDataConfirmationDialog.value = false
                    viewModel.onRestoreBackupClicked(context)
                },
            )
        }

        if (showBottomSheet) {
            WishAppBottomSheetM3(
                onDismissRequest = { showBottomSheet = false },
                sheetState = modalBottomSheetState,
            ) {
                BackupSheetContent(
                    modifier = Modifier.padding(bottom = 48.dp)
                )
            }
        }

        LoadingView(loadingState = loadingState)
    }
}

@Composable
private fun LoadingView(loadingState: LoadingState) {
    when (loadingState) {
        LoadingState.None -> {
        }

        LoadingState.Empty -> {
            FullscreenLoaderWithText(isTranslucent = false)
        }

        LoadingState.CheckingBackup -> {
            FullscreenLoaderWithText(
                isTranslucent = true,
                text = stringResource(R.string.backup_check_backup_loader_text),
            )
        }

        LoadingState.RestoringBackup -> {
            FullscreenLoaderWithText(
                isTranslucent = true,
                text = stringResource(R.string.backup_restore_backup_loader_text),
            )
        }

        LoadingState.UploadingNewBackup -> {
            FullscreenLoaderWithText(
                isTranslucent = true,
                text = stringResource(R.string.backup_create_backup_loader_text),
            )
        }
    }
}

@Composable
private fun DrivePermissionRationaleView(onGiveDrivePermissionClicked: () -> Unit) {

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.backup_drive_permission_rationale_text),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(onClick = onGiveDrivePermissionClicked) {
            Text(stringResource(R.string.backup_allow_access_to_drive_text))
        }
    }
}

@Composable
private fun CurrentBackupView(
    backupInfo: BackupInfo = BackupInfo.None,
    onCreateBackupClicked: () -> Unit,
    onRefreshBackupInfoClicked: () -> Unit
) {

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.backup_current_backup_info_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .weight(6f)
            )

            if (backupInfo is BackupInfo.Value) {
                FilledTonalIconButton(
                    modifier = Modifier
                        .size(26.dp)
                        .weight(1f),
                    onClick = onRefreshBackupInfoClicked
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }

        when (backupInfo) {
            is BackupInfo.None -> {
                ContentText(
                    text = stringResource(R.string.backup_no_backup_text),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            is BackupInfo.Value -> {
                if (backupInfo.accountEmail != null) {

                    ContentText(
                        text = stringResource(R.string.backup_current_backup_info_account, backupInfo.accountEmail),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                ContentText(
                    text = stringResource(
                        R.string.backup_current_backup_info_date,
                        BackupDateTimeFormatter.formatBackupDateTime(backupInfo.modifiedDateTime)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ContentText(
                    text = stringResource(
                        R.string.backup_current_backup_info_size,
                        BytesSizeFormatter.format(backupInfo.sizeInBytes)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (backupInfo.device != null) {
                    ContentText(
                        text = stringResource(R.string.backup_current_backup_info_device, backupInfo.device),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
        Button(onClick = { onCreateBackupClicked() }) {
            Text(stringResource(R.string.backup_create_backup_button_text))
        }
    }
}

@Composable
private fun RestoreBackupView(onRestoreBackupClicked: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.backup_restore_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ContentText(
            text = stringResource(R.string.backup_restore_info_text),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(onClick = { onRestoreBackupClicked() }) {
            Text(text = stringResource(R.string.backup_restore_button_text))
        }
    }
}

@Composable
private fun ForceUpdateAppDataView(onForceUpdateClicked: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.backup_restore_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ContentText(
            text = stringResource(R.string.backup_force_update_info_text),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedButton(onClick = { onForceUpdateClicked() }) {
            Text(text = stringResource(R.string.backup_force_update_button_text))
        }
    }
}

@Composable
private fun ContentText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Text(
        modifier = modifier,
        text = text,
        color = color,
        style = style,
    )
}