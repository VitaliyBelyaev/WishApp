package ru.vitaliy.belyaev.wishapp.ui.screens.settings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.ui.core.bottomsheet.WishAppBottomSheetM3
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.components.BackupSheetContent
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.components.SettingBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.components.ThemeSettingBlock
import ru.vitaliy.belyaev.wishapp.ui.theme.CommonColors
import ru.vitaliy.belyaev.wishapp.utils.createSharePlainTextIntent
import ru.vitaliy.belyaev.wishapp.utils.openGooglePlay
import ru.vitaliy.belyaev.wishapp.utils.trackScreenShow

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    onAboutAppClicked: () -> Unit,
    onBackupAndRestoreClicked: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val selectedTheme: Theme by viewModel.selectedTheme.collectAsState()
    val scrollState: ScrollState = rememberScrollState()
    val systemUiController = rememberSystemUiController()

    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    trackScreenShow { viewModel.trackScreenShow() }

    systemUiController.setNavigationBarColor(color = CommonColors.navBarColor())

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.Companion.safeDrawing,
        topBar = {
            WishAppTopBar(
                stringResource(R.string.settings),
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
        ) {
            Text(
                text = stringResource(R.string.theme_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
            )
            ThemeSettingBlock(
                selectedTheme = selectedTheme,
                onThemeClicked = { viewModel.onThemeItemClicked(it) }
            )
            Text(
                text = stringResource(R.string.other),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
            )
            SettingBlock(
                title = stringResource(R.string.backup),
                onClick = {
//                    viewModel.onBackupAndRestoreItemClicked()
//                    showBottomSheet = true
                    onBackupAndRestoreClicked()
                }
            )
            SettingBlock(
                title = stringResource(R.string.rate_app),
                onClick = {
                    viewModel.onRateAppItemClicked()
                    context.openGooglePlay()
                }
            )
            val shareAppText = stringResource(id = R.string.share_app_text)
            SettingBlock(
                title = stringResource(R.string.share_app),
                onClick = {
                    viewModel.onShareAppItemClicked()
                    context.startActivity(createSharePlainTextIntent(shareAppText))
                }
            )
            SettingBlock(
                title = stringResource(R.string.about_app),
                onClick = { onAboutAppClicked() }
            )
        }

        if (showBottomSheet) {
            WishAppBottomSheetM3(
                onDismissRequest = { showBottomSheet = false },
                sheetState = modalBottomSheetState,
            ) {
                BackupSheetContent(
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen({}, {}, {})
}