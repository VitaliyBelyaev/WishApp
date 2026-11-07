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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.navigation.AppNavigation
import ru.vitaliy.belyaev.wishapp.navigation.old.Navigation
import ru.vitaliy.belyaev.wishapp.navigation.old.Navigator
import ru.vitaliy.belyaev.wishapp.ui.components.dock.FloatingDockHeight
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
    AppTheme(selectedTheme = selectedTheme, useWallpaperColors = useWallpaperColors) {
        val snackbarHostState = remember { SnackbarHostState() }
        CollectAppSnackMessages(messages = appViewModel.showSnackOnMainFlow, hostState = snackbarHostState)

        // Surface задаёт фон и цвет текста по умолчанию для всех экранов нового вида
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavigation(
                    selectedTheme = selectedTheme,
                    analyticsRepository = analyticsRepository,
                    onShowMessage = appViewModel::showSnackMessageOnMain,
                    modifier = Modifier.fillMaxSize(),
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
