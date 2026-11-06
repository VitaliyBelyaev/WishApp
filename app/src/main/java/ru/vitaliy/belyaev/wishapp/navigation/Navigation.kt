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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp.AboutAppScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp.privacypolicy.PrivacyPolicyScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.EditTagsScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_images.WishImagesViewerScreenRoute
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.WishListScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed.WishDetailedScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.WishTagsScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
internal fun Navigation(
    analyticsRepository: AnalyticsRepository,
    navigator: Navigator,
) {

    val popBackStack: () -> Unit = {
        navigator.goBack()
    }
    val entryProvider = entryProvider {
        entry<MainRoute> {
            WishListScreen(
                openWishDetailed = { navigator.navigate(WishDetailedRoute(wishId = it.id)) },
                onAddWishClicked = { tagId ->
                    navigator.navigate(WishDetailedRoute(tagId = tagId))
                },
                onSettingIconClicked = { navigator.navigate(SettingsRoute) },
                onEditTagClick = { navigator.navigate(EditTagRoute) },
                onGoToBackupScreenClicked = { navigator.navigate(BackupAndRestoreRoute) },
            )
        }
        entry<WishDetailedRoute> { route ->
            WishDetailedScreen(
                wishId = route.wishId,
                tagId = route.tagId,
                sharedDraft = route.sharedDraftOrNull(),
                onBackPressed = popBackStack,
                onWishTagsClicked = { navigator.navigate(WishTagsRoute(it)) },
                onWishImageClicked = {
                    val imageRoute = WishImagesViewerRoute(
                        wishId = it.wishId,
                        wishImageIndex = it.wishImageIndex,
                    )
                    navigator.navigate(imageRoute)
                }
            )
        }
        entry<SettingsRoute> {
            SettingsScreen(
                onBackPressed = popBackStack,
                onAboutAppClicked = { navigator.navigate(AboutAppRoute) },
                onBackupAndRestoreClicked = { navigator.navigate(BackupAndRestoreRoute) },
            )
        }
        entry<BackupAndRestoreRoute> {
            BackupScreen(
                onBackPressed = popBackStack,
                analyticsRepository = analyticsRepository,
            )
        }
        entry<AboutAppRoute> {
            AboutAppScreen(
                onBackPressed = popBackStack,
                onPrivacyPolicyClicked = { navigator.navigate(PrivacyPolicyRoute) }
            )
        }
        entry<PrivacyPolicyRoute> {
            PrivacyPolicyScreen(
                onBackPressed = popBackStack
            )
        }
        entry<WishTagsRoute> { route ->
            WishTagsScreen(
                wishId = route.wishId,
                onBackPressed = popBackStack
            )
        }
        entry<EditTagRoute> {
            EditTagsScreen(
                onBackPressed = popBackStack
            )
        }
        entry<WishImagesViewerRoute> { route ->
            WishImagesViewerScreenRoute(
                wishId = route.wishId,
                initialWishImageIndex = route.wishImageIndex,
                onBackPressed = popBackStack
            )
        }
    }

    val animDuration = 400
    val tweenEasing = FastOutSlowInEasing
    val animSpec = tween<IntOffset>(
        durationMillis = animDuration,
        easing = tweenEasing
    )
    val smallOffset: (Int) -> Int = {
        -it / 5
    }

    NavDisplay(
        entries = navigator.state.toEntries(entryProvider),
        onBack = { navigator.goBack() },
        transitionSpec = {
            // Новый экран наезжает сверху, старый смещается влево
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = animSpec
            ) togetherWith slideOutHorizontally(
                targetOffsetX = smallOffset, // Старый экран смещается только на треть
                animationSpec = animSpec
            )
        },
        popTransitionSpec = {
            // При возврате: новый экран выезжает вправо, старый сползает сверху
            slideInHorizontally(
                initialOffsetX = smallOffset, // Смещенный экран возвращается
                animationSpec = animSpec
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it }, // Верхний экран уезжает вправо
                animationSpec = animSpec
            )
        },
        predictivePopTransitionSpec = {
            // Аналогично popTransitionSpec для predictive back
            slideInHorizontally(
                initialOffsetX = smallOffset,
                animationSpec = animSpec
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = animSpec
            )
        },
    )
}
