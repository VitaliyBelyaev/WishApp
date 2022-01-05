package ru.vitaliy.belyaev.wishapp.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.components.BackupSheetContent
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.components.SettingBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.components.ThemeSettingBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.entity.Backup
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.entity.SettingItem
import ru.vitaliy.belyaev.wishapp.utils.openGooglePlay

@ExperimentalMaterialApi
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    onAboutAppClicked: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val settingItem: MutableState<SettingItem> = remember { mutableStateOf(Backup) }
    val selectedTheme: Theme by viewModel.selectedTheme.collectAsState()

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        scrimColor = Color.Black.copy(alpha = 0.32f),
        sheetContent = {
            when (settingItem.value) {
                is Backup -> BackupSheetContent(modalBottomSheetState)
                else -> {
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                WishAppTopBar(
                    stringResource(R.string.settings),
                    withBackIcon = true,
                    onBackPressed = onBackPressed
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.theme_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
                    )
                    ThemeSettingBlock(
                        selectedTheme = selectedTheme,
                        onThemeClicked = {
                            Firebase.analytics.logEvent("select_app_theme") {
                                param("theme", it.name)
                            }
                            viewModel.updateSelectedTheme(it)
                        }
                    )
                    Text(
                        text = stringResource(R.string.other),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
                    )
                    SettingBlock(
                        title = stringResource(R.string.backup),
                        onClick = {
                            Firebase.analytics.logEvent("about_data_backup_click", null)
                            settingItem.value = Backup
                            scope.launch {
                                modalBottomSheetState.show()
                            }
                        }
                    )
                    SettingBlock(
                        title = stringResource(R.string.rate_app),
                        onClick = {
                            Firebase.analytics.logEvent("rate_app_click", null)
                            context.openGooglePlay()
                        }
                    )
                    SettingBlock(
                        title = stringResource(R.string.about_app),
                        onClick = { onAboutAppClicked() }
                    )
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen({}, {})
}