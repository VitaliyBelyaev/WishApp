package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
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
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.core.loader.FullscreenLoaderWithText
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.theme.CommonColors
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
        Box(
            modifier = Modifier
                .padding(pd)
                .fillMaxSize()
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Последнее резервное копирование",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Google Drive: ${BackupDateTimeFormatter.formatBackupDateTime(state.backupInfo.createdDateTime)}",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Обём: ${state.backupInfo.sizeInBytes / 1000} КB",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(onClick = { viewModel.onCreateBackupClicked(context) }) {
                            Text(text = "Создать резервную копию")
                        }
                    }
                }

                is BackupViewState.CurrentBackupWithRestore -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Последнее резервное копирование", modifier = Modifier.padding(bottom = 8.dp))
                        Text(
                            text = "Google Drive: ${BackupDateTimeFormatter.formatBackupDateTime(state.backupInfo.createdDateTime)}",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Обём: ${state.backupInfo.sizeInBytes / 1000} КB",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(onClick = { viewModel.onCreateBackupClicked(context) }) {
                            Text(text = "Создать резервную копию")
                        }

                        Text(text = "Восстановление", modifier = Modifier.padding(bottom = 8.dp, top = 16.dp))
                        Button(onClick = { viewModel.onRestoreBackupClicked(context) }) {
                            Text(text = "Восстановить данные из копии")
                        }
                    }
                }

                is BackupViewState.DrivePermissionRationale -> {
                    DrivePermissionRationale {
                        drivePermissionLauncher.launch(viewModel.signInIntent)
                        viewModel.onGiveDrivePermissionClicked()
                    }
                }

                is BackupViewState.NoBackup -> {}
            }

            when (loadingState) {
                LoadingState.Empty -> {
                    FullscreenLoaderWithText(isTranslucent = false)
                }

                LoadingState.None -> {
                }

                LoadingState.OverData -> {
                    FullscreenLoaderWithText(isTranslucent = true)
                }
            }
        }

    }
}

@Composable
private fun DrivePermissionRationale(onGiveDrivePermissionClicked: () -> Unit) {

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
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