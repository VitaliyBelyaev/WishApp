package ru.vitaliy.belyaev.wishapp.navigation

import ru.vitaliy.belyaev.wishapp.ui.old.theme.WishAppTheme as OldWishAppTheme
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.Serializable
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.navigation.old.AboutAppRoute
import ru.vitaliy.belyaev.wishapp.navigation.old.BackupAndRestoreRoute
import ru.vitaliy.belyaev.wishapp.navigation.old.EditTagRoute
import ru.vitaliy.belyaev.wishapp.navigation.old.PrivacyPolicyRoute
import ru.vitaliy.belyaev.wishapp.navigation.old.SettingsRoute
import ru.vitaliy.belyaev.wishapp.navigation.old.WishDetailedRoute
import ru.vitaliy.belyaev.wishapp.navigation.old.WishImagesViewerRoute
import ru.vitaliy.belyaev.wishapp.navigation.old.WishTagsRoute
import ru.vitaliy.belyaev.wishapp.ui.old.screens.aboutapp.AboutAppScreen
import ru.vitaliy.belyaev.wishapp.ui.old.screens.aboutapp.privacypolicy.PrivacyPolicyScreen
import ru.vitaliy.belyaev.wishapp.ui.old.screens.backup.BackupScreen
import ru.vitaliy.belyaev.wishapp.ui.old.screens.edittags.EditTagsScreen
import ru.vitaliy.belyaev.wishapp.ui.old.screens.settings.SettingsScreen
import ru.vitaliy.belyaev.wishapp.ui.old.screens.wish_images.WishImagesViewerScreenRoute
import ru.vitaliy.belyaev.wishapp.ui.old.screens.wishdetailed.WishDetailedScreen
import ru.vitaliy.belyaev.wishapp.ui.old.screens.wishtags.WishTagsScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.WishListActions
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.WishListScreen

/**
 * Навигация нового вида. Прежний вид живёт в `navigation/old` со своим стеком,
 * при смене вида стек начинается заново со списка.
 */
@Serializable
data object WishListRoute : NavKey

private const val TransitionMillis = 300

/**
 * Список нового вида и экраны, которых в новом виде ещё нет. Они открываются в прежнем виде,
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
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(WishListRoute)
    val navigate: (NavKey) -> Unit = { backStack.add(it) }
    val goBack: () -> Unit = { backStack.removeLastOrNull() }

    // Экран прежнего вида в своей теме
    val old: @Composable (@Composable () -> Unit) -> Unit = { content ->
        OldWishAppTheme(selectedTheme = selectedTheme, content = content)
    }

    val entryProvider = entryProvider {
        entry<WishListRoute> {
            WishListScreen(
                actions = WishListActions(
                    onWishClick = { navigate(WishDetailedRoute(wishId = it)) },
                    onAddWish = { tagId -> navigate(WishDetailedRoute(tagId = tagId)) },
                    onCreateFromSearch = { title -> navigate(WishDetailedRoute(sharedTitle = title)) },
                    onSettings = { navigate(SettingsRoute) },
                    onManageTags = { navigate(EditTagRoute) },
                    onRestore = { navigate(BackupAndRestoreRoute) },
                    onShowMessage = onShowMessage,
                ),
            )
        }
        entry<WishDetailedRoute> { route ->
            old {
                WishDetailedScreen(
                    wishId = route.wishId,
                    tagId = route.tagId,
                    sharedDraft = route.sharedDraftOrNull(),
                    onBackPressed = goBack,
                    onWishTagsClicked = { navigate(WishTagsRoute(it)) },
                    onWishImageClicked = {
                        navigate(WishImagesViewerRoute(wishId = it.wishId, wishImageIndex = it.wishImageIndex))
                    },
                )
            }
        }
        entry<SettingsRoute> {
            old {
                SettingsScreen(
                    onBackPressed = goBack,
                    onAboutAppClicked = { navigate(AboutAppRoute) },
                    onBackupAndRestoreClicked = { navigate(BackupAndRestoreRoute) },
                )
            }
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
        entry<WishTagsRoute> { route ->
            old { WishTagsScreen(wishId = route.wishId, onBackPressed = goBack) }
        }
        entry<EditTagRoute> {
            old { EditTagsScreen(onBackPressed = goBack) }
        }
        entry<WishImagesViewerRoute> { route ->
            old {
                WishImagesViewerScreenRoute(
                    wishId = route.wishId,
                    initialWishImageIndex = route.wishImageIndex,
                    onBackPressed = goBack,
                )
            }
        }
    }

    val offsetSpec = tween<androidx.compose.ui.unit.IntOffset>(TransitionMillis, easing = FastOutSlowInEasing)
    val fadeSpec = tween<Float>(TransitionMillis, easing = FastOutSlowInEasing)
    val smallOffset: (Int) -> Int = { it / 5 }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { goBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider,
        transitionSpec = {
            (slideInHorizontally(offsetSpec) { smallOffset(it) } + fadeIn(fadeSpec)) togetherWith
                (slideOutHorizontally(offsetSpec) { -smallOffset(it) } + fadeOut(fadeSpec))
        },
        popTransitionSpec = {
            (slideInHorizontally(offsetSpec) { -smallOffset(it) } + fadeIn(fadeSpec)) togetherWith
                (slideOutHorizontally(offsetSpec) { smallOffset(it) } + fadeOut(fadeSpec))
        },
        predictivePopTransitionSpec = {
            (slideInHorizontally(offsetSpec) { -smallOffset(it) } + fadeIn(fadeSpec)) togetherWith
                (slideOutHorizontally(offsetSpec) { smallOffset(it) } + fadeOut(fadeSpec))
        },
    )
}
