package ru.vitaliy.belyaev.wishapp.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.navigation.AppNavigation
import ru.vitaliy.belyaev.wishapp.navigation.old.Navigation
import ru.vitaliy.belyaev.wishapp.navigation.old.Navigator
import ru.vitaliy.belyaev.wishapp.ui.components.dock.FloatingDockHeight
import ru.vitaliy.belyaev.wishapp.ui.components.reveal.LocalThemeReveal
import ru.vitaliy.belyaev.wishapp.ui.components.reveal.rememberThemeRevealState
import ru.vitaliy.belyaev.wishapp.ui.components.reveal.themeReveal
import ru.vitaliy.belyaev.wishapp.ui.core.snackbar.AppSnackbarHost
import ru.vitaliy.belyaev.wishapp.ui.core.snackbar.CollectAppSnackMessages
import ru.vitaliy.belyaev.wishapp.ui.old.theme.WishAppTheme as OldWishAppTheme
import ru.vitaliy.belyaev.wishapp.ui.theme.AppTheme
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing

/** Прежний вид: своя тема, навигация и снэкбары на каждом экране. */
@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
internal fun OldLookRoot(
    selectedTheme: Theme,
    analyticsRepository: AnalyticsRepository,
    navigator: Navigator,
) {
    OldWishAppTheme(selectedTheme = selectedTheme) {
        Navigation(
            analyticsRepository = analyticsRepository,
            navigator = navigator,
        )
    }
}

/**
 * Новый вид: своя тема и навигация, один снэкбар над навигацией. Поэтому снэкбар и отмена
 * переживают переход между экранами.
 */
@Composable
internal fun NewLookRoot(
    selectedTheme: Theme,
    useWallpaperColors: Boolean,
    analyticsRepository: AnalyticsRepository,
    appViewModel: AppActivityViewModel,
) {
    val reveal = rememberThemeRevealState()
    AppTheme(
        selectedTheme = selectedTheme,
        useWallpaperColors = useWallpaperColors,
        holdSystemBars = reveal.isCoveringTop,
    ) {
        val incomingShare by appViewModel.incomingShare.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        CollectAppSnackMessages(messages = appViewModel.showSnackOnMainFlow, hostState = snackbarHostState)

        val appearance = MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.primary
        SideEffect { reveal.appearance = appearance }

        // Surface задаёт фон и цвет текста по умолчанию для всех экранов нового вида.
        // Через themeReveal смена темы в настройках расходится кругом от кнопки
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .themeReveal(reveal),
            color = MaterialTheme.colorScheme.surface,
        ) {
            CompositionLocalProvider(LocalThemeReveal provides reveal) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        selectedTheme = selectedTheme,
                        analyticsRepository = analyticsRepository,
                        onShowMessage = appViewModel::showSnack,
                        modifier = Modifier.fillMaxSize(),
                        incomingShare = incomingShare,
                        onIncomingShareHandled = { appViewModel.consumeIncomingShare() },
                    )
                    // Над плавающей панелью списка. На экранах без панели снэкбар тоже стоит на этой высоте
                    AppSnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = FloatingDockHeight + WishAppSpacing.s3 * 2),
                    )
                }
            }
        }
    }
}
