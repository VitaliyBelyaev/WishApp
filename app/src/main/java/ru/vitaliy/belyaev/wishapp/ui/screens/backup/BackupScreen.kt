package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import java.time.format.DateTimeFormatter
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.theme.CommonColors
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
    onBackupClicked: () -> Unit,
    onCheckBackupClicked: () -> Unit,
    onRestoreClicked: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel(),
    appViewModel: AppActivityViewModel = hiltViewModel(LocalContext.current as AppActivity),
) {

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val systemUiController = rememberSystemUiController()
    val scrollState: ScrollState = rememberScrollState()


    trackScreenShow { viewModel.trackScreenShow() }

    systemUiController.setNavigationBarColor(color = CommonColors.navBarColor())

    val backupInfo: BackupInfo by appViewModel.currentBackupInfo.collectAsState()

// todo сделать GoggleSignIn нормально
    val startForResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val intent = result.data
//            if (result.data != null) {
//                val task: Task<GoogleSignInAccount> =
//                    GoogleSignIn.getSignedInAccountFromIntent(intent)
//
//                /**
//                 * handle [task] result
//                 */
//            } else {
//                Toast.makeText(ctx, "Google Login Error!", Toast.LENGTH_LONG).show()
//            }
//        }
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
                .padding(pd)
                .padding(16.dp)
        ) {
            Text(
                text = "Last backup",
//                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            Button(
                onClick = {
                    onBackupClicked()
                },
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            ) {
                Text(text = "Do backup")
            }

            Button(
                onClick = {
                    onCheckBackupClicked()
                },
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            ) {
                Text(text = "Check existing backup")
            }


            when (val backup = backupInfo) {
                BackupInfo.None -> {
                }

                is BackupInfo.Value -> {
                    Text(
                        text = "Last modified: ${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(backup.createdDateTime)}",
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )

                    Text(
                        text = "Size: ${backup.sizeInBytes / 1024} KB",
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )

                    Button(
                        onClick = {
                            onRestoreClicked()
                        },
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    ) {
                        Text(text = "Restore")
                    }
                }
            }
        }

    }
}