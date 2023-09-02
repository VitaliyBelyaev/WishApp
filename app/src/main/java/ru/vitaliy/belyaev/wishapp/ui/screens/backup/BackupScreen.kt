package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.core.loader.FullscreenLoaderWithText
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.theme.CommonColors
import ru.vitaliy.belyaev.wishapp.utils.BytesSizeFormatter
import ru.vitaliy.belyaev.wishapp.utils.getDataOrNull
import ru.vitaliy.belyaev.wishapp.utils.trackScreenShow
import timber.log.Timber

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
internal fun BackupScreen(
    onBackPressed: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel(),
    appViewModel: AppActivityViewModel = hiltViewModel(LocalContext.current as AppActivity),
) {

    val viewState by viewModel.viewState.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val systemUiController = rememberSystemUiController()
    val scrollState: ScrollState = rememberScrollState()

    trackScreenShow { viewModel.trackScreenShow() }

    systemUiController.setNavigationBarColor(color = CommonColors.navBarColor())

    val drivePermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.onSignInResultReceived(result.getDataOrNull())
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
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { pd ->
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(pd)
        ) {
            Timber.tag("RTRT").d("BackupScreen: viewState = $viewState")

            when (val state = viewState) {
                is BackupViewState.None -> {
                }

                is BackupViewState.CheckBackupError -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Не удалось проверить есть ли резервная копия",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(onClick = { viewModel.onRetryCheckBackupClicked() }) {
                            Text(text = "Попробовать снова")
                        }
                    }
                }

                is BackupViewState.CurrentBackup -> {
                    CurrentBackupView(
                        backupInfo = state.backupInfo,
                        onCreateBackupClicked = { viewModel.onCreateBackupClicked(context) }
                    )
                }

                is BackupViewState.CurrentBackupWithRestore -> {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                    )
                    CurrentBackupView(
                        backupInfo = state.backupInfo,
                        onCreateBackupClicked = { viewModel.onCreateBackupClicked(context) }
                    )
                    Divider(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp))
                    RestoreBackupView(
                        onRestoreBackupClicked = { viewModel.onRestoreBackupClicked(context) }
                    )
                }

                is BackupViewState.DrivePermissionRationale -> {
                    DrivePermissionRationaleView {
                        drivePermissionLauncher.launch(viewModel.signInIntent)
                        viewModel.onGiveDrivePermissionClicked()
                    }
                }

                is BackupViewState.NoBackup -> {}
            }
        }


        when (loadingState) {
            LoadingState.None -> {
            }

            LoadingState.Empty -> {
                FullscreenLoaderWithText(isTranslucent = false)
            }

            LoadingState.CheckingBackup -> {
                FullscreenLoaderWithText(
                    isTranslucent = true,
                    text = "Проверяем наличие резервной копии"
                )
            }

            LoadingState.RestoringBackup -> {
                FullscreenLoaderWithText(
                    isTranslucent = true,
                    text = "Восстанавливаем данные из резервной копии"
                )
            }

            LoadingState.UploadingNewBackup -> {
                FullscreenLoaderWithText(
                    isTranslucent = true,
                    text = "Создаём резервную копию"
                )
            }
        }
    }
}

@Composable
private fun DrivePermissionRationaleView(onGiveDrivePermissionClicked: () -> Unit) {

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Мы используем Google Drive для сохранения и восстановления ваших данных в приложении. Для этого нам нужно разрешение использовать ваш Google Drive.\n" +
                    "\n" +
                    "Не волнуйтесь, у нас не будет доступа к вашим файлам в Google Drive, кроме файла с данными из приложения, который мы будем создавать.",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(onClick = onGiveDrivePermissionClicked, modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) {
            Text(text = "Разрешить доступ к Google Drive")
        }
    }
}

@Composable
private fun CurrentBackupView(
    backupInfo: BackupInfo.Value,
    onCreateBackupClicked: () -> Unit
) {

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Последнее резервное копирование",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Google Drive: ${BackupDateTimeFormatter.formatBackupDateTime(backupInfo.modifiedDateTime)}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Объём: ${BytesSizeFormatter.format(backupInfo.sizeInBytes)}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(onClick = { onCreateBackupClicked() }) {
            Text(text = "Создать резервную копию")
        }
    }
}

@Composable
private fun RestoreBackupView(
    onRestoreBackupClicked: () -> Unit
) {

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Восстановление",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "У вас есть резервная копия, но вы не восстанавливали данные из неё на этом устройстве.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(onClick = { onRestoreBackupClicked() }) {
            Text(text = "Восстановить данные из копии")
        }
    }
}