package ru.vitaliy.belyaev.wishapp.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        // Над нижней панелью снэкбар поднят, на экранах без неё стоит у края. Сообщает навигация
        var isSnackbarLifted by remember { mutableStateOf(true) }
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
                        onShowMessage = appViewModel::showSnack,
                        modifier = Modifier.fillMaxSize(),
                        incomingShare = incomingShare,
                        onIncomingShareHandled = { appViewModel.consumeIncomingShare() },
                        onSnackbarLiftChange = { isSnackbarLifted = it },
                    )
                    // Над доком списка и панелью хотелки, а на экранах без нижней панели — у края.
                    // Открытая клавиатура (переименование ярлыка) поднимает его над собой
                    val snackbarLift by animateDpAsState(
                        targetValue = if (isSnackbarLifted) SnackAboveDock else WishAppSpacing.s3,
                        label = "snackbarLift",
                    )
                    AppSnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                            .padding(bottom = snackbarLift),
                    )
                }
            }
        }
    }
}

/** Над плавающим доком списка: его высота и отступы под ним и над ним. */
private val SnackAboveDock = FloatingDockHeight + WishAppSpacing.s3 * 2
