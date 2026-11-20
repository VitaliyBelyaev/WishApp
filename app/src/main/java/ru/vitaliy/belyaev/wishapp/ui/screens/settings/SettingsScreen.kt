package ru.vitaliy.belyaev.wishapp.ui.screens.settings

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.model.UiLook
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.components.header.PageHeader
import ru.vitaliy.belyaev.wishapp.ui.components.header.PageHeaderCollapse
import ru.vitaliy.belyaev.wishapp.ui.components.header.headerHeight
import ru.vitaliy.belyaev.wishapp.ui.components.reveal.LocalThemeReveal
import ru.vitaliy.belyaev.wishapp.ui.components.reveal.changeOrApply
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.isWallpaperColorsAvailable
import ru.vitaliy.belyaev.wishapp.utils.createSharePlainTextIntent
import ru.vitaliy.belyaev.wishapp.utils.openGooglePlay
import ru.vitaliy.belyaev.wishapp.utils.trackScreenShow

/** Куда ведут строки настроек. «О приложении» пока открывается прежним экраном. */
class SettingsActions(
    val onBack: () -> Unit,
    val onBackup: () -> Unit,
    val onManageTags: () -> Unit,
    val onAbout: () -> Unit,
)

/**
 * Настройки нового вида (`ScreenSettings` в дизайн-системе, экран `settings` в прототипе): вид приложения,
 * тема, цвет, данные и «О приложении» одним экраном. Выбор вида меняет всё дерево экранов, поэтому
 * живёт в [AppActivityViewModel].
 */
@Composable
fun SettingsScreen(
    actions: SettingsActions,
    viewModel: SettingsViewModel = hiltViewModel(),
    appViewModel: AppActivityViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isLookChoiceAvailable by appViewModel.isUiLookChoiceAvailable.collectAsStateWithLifecycle()
    val shareAppText = stringResource(R.string.share_app_text)
    val reveal = LocalThemeReveal.current
    val isSystemDark = isSystemInDarkTheme()
    val isDark = AppThemeExtras.isDark

    trackScreenShow { viewModel.trackScreenShow() }

    SettingsContent(
        state = state,
        showLookChoice = isLookChoiceAvailable,
        showColorChoice = isWallpaperColorsAvailable(),
        callbacks = object : SettingsCallbacks {
            override fun onBack() = actions.onBack()
            override fun onClassicLook() = appViewModel.onUiLookChosen(UiLook.OLD)
            override fun onTheme(theme: Theme, origin: Offset) {
                val becomesDark = when (theme) {
                    Theme.SYSTEM -> isSystemDark
                    Theme.DARK -> true
                    Theme.LIGHT -> false
                }
                reveal.changeOrApply(origin, changesLook = becomesDark != isDark) { viewModel.onThemeSelected(theme) }
            }
            override fun onWallpaperColors(useWallpaperColors: Boolean, origin: Offset) {
                val changesLook = useWallpaperColors != state?.useWallpaperColors
                reveal.changeOrApply(origin, changesLook) { viewModel.onWallpaperColorsSelected(useWallpaperColors) }
            }
            override fun onBackup() {
                viewModel.onBackupClicked()
                actions.onBackup()
            }
            override fun onManageTags() = actions.onManageTags()
            override fun onRate() {
                viewModel.onRateClicked()
                context.openGooglePlay()
            }
            override fun onShareApp() {
                viewModel.onShareAppClicked()
                context.startActivity(createSharePlainTextIntent(shareAppText))
            }
            override fun onAbout() = actions.onAbout()
        },
    )
}

internal interface SettingsCallbacks {
    fun onBack()
    fun onClassicLook()
    fun onTheme(theme: Theme, origin: Offset)
    fun onWallpaperColors(useWallpaperColors: Boolean, origin: Offset)
    fun onBackup()
    fun onManageTags()
    fun onRate()
    fun onShareApp()
    fun onAbout()
}

/**
 * @param state `null`, пока тема и цвет не прочитаны: блоки выбора не рисуются, чтобы не мигнул
 * выбор по умолчанию.
 * @param showColorChoice цвета из обоев есть только на Android 12+. Ниже блок «Цвет» не показываем:
 * из одной «Хурмы» выбирать нечего.
 */
@Composable
internal fun SettingsContent(
    state: SettingsState?,
    showLookChoice: Boolean,
    showColorChoice: Boolean,
    callbacks: SettingsCallbacks,
) {
    val scrollState = rememberScrollState()
    val collapsePx = with(LocalDensity.current) { PageHeaderCollapse.toPx() }
    val progress: () -> Float = { (scrollState.value / collapsePx).coerceIn(0f, 1f) }
    // Фон свой: при предиктивном «назад» экран уезжает, и без фона сквозь него был виден список
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(start = WishAppSpacing.s4, end = WishAppSpacing.s4, top = headerHeight() + WishAppSpacing.s2)
                .testTag(SettingsTestTags.CONTENT),
        ) {
            if (showLookChoice) {
                SectionLabel(stringResource(R.string.look_title), isFirst = true)
                LookTiles(onClassic = callbacks::onClassicLook)
            }
            if (state != null) {
                SectionLabel(stringResource(R.string.theme_title), isFirst = !showLookChoice)
                ThemeSegments(selected = state.theme, onSelect = callbacks::onTheme)
                if (showColorChoice) {
                    SectionLabel(stringResource(R.string.color_title))
                    ColorTiles(useWallpaperColors = state.useWallpaperColors, onSelect = callbacks::onWallpaperColors)
                }
            }
            SectionLabel(stringResource(R.string.settings_data))
            SettingsList {
                SettingsRow(
                    icon = R.drawable.ic_ms_cloud_done,
                    title = stringResource(R.string.backup_title),
                    onClick = callbacks::onBackup,
                )
                SettingsRow(
                    icon = R.drawable.ic_ms_tune,
                    title = stringResource(R.string.tags_manage_title),
                    subtitle = stringResource(R.string.settings_tags_sub),
                    onClick = callbacks::onManageTags,
                )
            }
            SectionLabel(stringResource(R.string.about_app))
            SettingsList {
                SettingsRow(
                    icon = R.drawable.ic_ms_star,
                    title = stringResource(R.string.settings_rate),
                    isExternal = true,
                    onClick = callbacks::onRate,
                )
                SettingsRow(
                    icon = R.drawable.ic_ms_share,
                    title = stringResource(R.string.settings_share),
                    isExternal = true,
                    onClick = callbacks::onShareApp,
                )
                SettingsRow(
                    icon = R.drawable.ic_ms_info,
                    title = stringResource(R.string.about_app),
                    onClick = callbacks::onAbout,
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
        PageHeader(title = stringResource(R.string.settings), progress = progress, onBack = callbacks::onBack)
    }
}

object SettingsTestTags {
    const val CONTENT = "settings_content"
    const val LOOK_NEW = "settings_look_new"
    const val LOOK_CLASSIC = "settings_look_classic"
}
