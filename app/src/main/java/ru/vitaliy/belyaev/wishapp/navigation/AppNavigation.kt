package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.Serializable
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.navigation.old.AboutAppRoute
import ru.vitaliy.belyaev.wishapp.navigation.old.BackupAndRestoreRoute
import ru.vitaliy.belyaev.wishapp.navigation.old.PrivacyPolicyRoute
import ru.vitaliy.belyaev.wishapp.navigation.old.SettingsRoute
import ru.vitaliy.belyaev.wishapp.ui.IncomingShare
import ru.vitaliy.belyaev.wishapp.ui.core.snackbar.AppSnackMessage
import ru.vitaliy.belyaev.wishapp.ui.old.screens.aboutapp.AboutAppScreen
import ru.vitaliy.belyaev.wishapp.ui.old.screens.aboutapp.privacypolicy.PrivacyPolicyScreen
import ru.vitaliy.belyaev.wishapp.ui.old.screens.backup.BackupScreen
import ru.vitaliy.belyaev.wishapp.ui.old.theme.WishAppTheme as OldWishAppTheme
import ru.vitaliy.belyaev.wishapp.ui.screens.photos.PhotoHeroLink
import ru.vitaliy.belyaev.wishapp.ui.screens.photos.PhotoViewerActions
import ru.vitaliy.belyaev.wishapp.ui.screens.photos.PhotoViewerScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsActions
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.tags.TagsManageActions
import ru.vitaliy.belyaev.wishapp.ui.screens.tags.TagsManageScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wish.WishScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wish.WishScreenActions
import ru.vitaliy.belyaev.wishapp.ui.screens.wish.WishScreenResult
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.WishListActions
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.WishListScreen

/**
 * Навигация нового вида. Прежний вид живёт в `navigation/old` со своим стеком,
 * при смене вида стек начинается заново со списка.
 */
@Serializable
data object WishListRoute : NavKey

/** Экран хотелки нового вида. Новая хотелка создаётся в шторке, сюда приходят только готовые. */
@Serializable
data class WishRoute(val wishId: String) : NavKey

/** Управление ярлыками нового вида: из настроек и из шторки ярлыков. */
@Serializable
data object TagsManageRoute : NavKey

/** Просмотр фото хотелки на весь экран, открывается с того фото, на которое нажали. */
@Serializable
data class PhotoViewerRoute(val wishId: String, val index: Int) : NavKey

private const val TRANSITION_MILLIS = 300

/**
 * Список и хотелка нового вида и экраны, которых в новом виде ещё нет. Они открываются в прежнем виде,
 * в своей теме, и уйдут по мере редизайна: ключи маршрутов общие со старой навигацией.
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterialApi::class,
)
@Composable
fun AppNavigation(
    selectedTheme: Theme,
    analyticsRepository: AnalyticsRepository,
    onShowMessage: (AppSnackMessage) -> Unit,
    modifier: Modifier = Modifier,
    incomingShare: IncomingShare? = null,
    onIncomingShareHandled: () -> Unit = {},
    onSnackbarLiftChange: (Boolean) -> Unit = {},
) {
    val backStack = rememberNavBackStack(WishListRoute)
    // Снэкбар поднят над нижней панелью списка, хотелки и просмотра фото, на остальных экранах её нет
    val top = backStack.lastOrNull()
    val isSnackbarLifted = top is WishListRoute || top is WishRoute || top is PhotoViewerRoute
    SideEffect { onSnackbarLiftChange(isSnackbarLifted) }
    // С чем экран хотелки вернулся на список: список дожидается записей и показывает отменяемое действие
    var wishScreenResult by remember { mutableStateOf<WishScreenResult?>(null) }
    // «Поделиться» из другого приложения: возвращаемся на список, шторку откроет он.
    // Открытая хотелка закрывается так же, как по «назад»
    DisposableEffect(incomingShare) {
        if (incomingShare != null) {
            backStack.filterIsInstance<WishRoute>().lastOrNull()?.let { wishScreenResult = WishScreenResult(it.wishId) }
            backStack.popToFirst()
        }
        onDispose { }
    }
    // Фото хотелки и просмотр знают друг о друге: фото вырастает из своего места и сжимается обратно
    val photoHeroLink = remember { PhotoHeroLink() }
    val navigate: (NavKey) -> Unit = { backStack.add(it) }
    val goBack: () -> Unit = { backStack.removeLastOrNull() }
    // Системный «назад», в том числе предиктивный: экран хотелки не перехватывает его и закрывается
    // сразу, а результат с простым «назад» отдаём списку здесь
    val systemBack: () -> Unit = {
        (backStack.lastOrNull() as? WishRoute)?.let { wishScreenResult = WishScreenResult(it.wishId) }
        goBack()
    }

    // Экран прежнего вида в своей теме
    val old: @Composable (@Composable () -> Unit) -> Unit = { content ->
        OldWishAppTheme(selectedTheme = selectedTheme, content = content)
    }

    val entryProvider = entryProvider {
        entry<WishListRoute> {
            WishListScreen(
                actions = WishListActions(
                    onWishClick = { wishId -> navigate(WishRoute(wishId = wishId)) },
                    onSettings = { navigate(SettingsRoute) },
                    onManageTags = { navigate(TagsManageRoute) },
                    onRestore = { navigate(BackupAndRestoreRoute) },
                    onShowMessage = onShowMessage,
                ),
                incomingShare = incomingShare,
                onIncomingShareHandled = onIncomingShareHandled,
                wishScreenResult = wishScreenResult,
                onWishScreenResultHandled = { wishScreenResult = null },
            )
        }
        entry<WishRoute> { route ->
            WishScreen(
                wishId = route.wishId,
                photoHeroLink = photoHeroLink,
                actions = wishScreenActions(
                    navigate = navigate,
                    goBack = goBack,
                    onShowMessage = onShowMessage,
                    onResult = { result -> wishScreenResult = result },
                ),
            )
        }
        entry<PhotoViewerRoute>(metadata = OverlaySceneStrategy.overlay()) { route ->
            PhotoViewerScreen(
                wishId = route.wishId,
                initialIndex = route.index,
                heroLink = photoHeroLink,
                actions = PhotoViewerActions(
                    // Просмотр сам доигрывает выход и только потом закрывается: убираем только его
                    onClose = { if (backStack.lastOrNull() == route) goBack() },
                    onShowMessage = onShowMessage,
                ),
            )
        }
        entry<SettingsRoute> {
            SettingsScreen(actions = settingsActions(navigate = navigate, goBack = goBack))
        }
        entry<TagsManageRoute> {
            TagsManageScreen(actions = TagsManageActions(onBack = goBack, onShowMessage = onShowMessage))
        }
        entry<BackupAndRestoreRoute> {
            old { BackupScreen(onBackPressed = goBack, analyticsRepository = analyticsRepository) }
        }
        entry<AboutAppRoute> {
            old { AboutAppScreen(onBackPressed = goBack, onPrivacyPolicyClicked = { navigate(PrivacyPolicyRoute) }) }
        }
        entry<PrivacyPolicyRoute> {
            old { PrivacyPolicyScreen(onBackPressed = goBack) }
        }
    }

    val offsetSpec = tween<IntOffset>(TRANSITION_MILLIS, easing = FastOutSlowInEasing)
    // Как в мессенджерах: новый экран целиком наезжает справа, прежний уходит влево на пятую часть.
    // Без затухания: при жесте «назад» верхний экран просто едет за пальцем
    val behind: (Int) -> Int = { -it / BEHIND_SHIFT_PART }
    val push = slideInHorizontally(offsetSpec) { it } togetherWith slideOutHorizontally(offsetSpec, behind)
    val pop = slideInHorizontally(offsetSpec, behind) togetherWith slideOutHorizontally(offsetSpec) { it }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = systemBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider,
        sceneStrategies = remember { listOf(OverlaySceneStrategy<NavKey>(), SinglePaneSceneStrategy()) },
        transitionSpec = { push },
        popTransitionSpec = { pop },
        predictivePopTransitionSpec = { pop },
    )
}

/** Насколько прежний экран уходит влево: на пятую часть ширины. */
private const val BEHIND_SHIFT_PART = 5

/** Результат экрана хотелки уходит на список, туда же и возврат. */
private fun wishScreenActions(
    navigate: (NavKey) -> Unit,
    goBack: () -> Unit,
    onShowMessage: (AppSnackMessage) -> Unit,
    onResult: (WishScreenResult) -> Unit,
) = WishScreenActions(
    onFinished = { result ->
        onResult(result)
        goBack()
    },
    onOpenPhoto = { wishId, index -> navigate(PhotoViewerRoute(wishId = wishId, index = index)) },
    onShowMessage = onShowMessage,
)

private fun MutableList<NavKey>.popToFirst() {
    while (size > 1) removeAt(lastIndex)
}

private fun settingsActions(navigate: (NavKey) -> Unit, goBack: () -> Unit) = SettingsActions(
    onBack = goBack,
    onBackup = { navigate(BackupAndRestoreRoute) },
    onManageTags = { navigate(TagsManageRoute) },
    onAbout = { navigate(AboutAppRoute) },
)
