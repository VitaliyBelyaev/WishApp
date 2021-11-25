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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.components.SettingBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.entity.Backup
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.entity.SettingItem
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.entity.Theme

@ExperimentalMaterialApi
@OptIn(ExperimentalUnitApi::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    onAboutAppClicked: () -> Unit
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val settingItem: MutableState<SettingItem> = remember { mutableStateOf(Theme) }

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        sheetContent = {
            when (settingItem.value) {
                is Theme -> ThemeSheetContent()
                is Backup -> BackupSheetContent()
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
                        text = stringResource(R.string.main_settings),
                        fontSize = TextUnit(14f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
                    )
                    SettingBlock(
                        title = stringResource(R.string.theme),
                        onClick = {
                            settingItem.value = Theme
                            scope.launch {
                                modalBottomSheetState.show()
                            }
                        }
                    )
                    SettingBlock(
                        title = stringResource(R.string.backup),
                        onClick = {
                            settingItem.value = Backup
                            scope.launch {
                                modalBottomSheetState.show()
                            }
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