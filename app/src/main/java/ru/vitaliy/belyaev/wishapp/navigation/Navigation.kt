package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
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
        entry<MainRoute2> {
            WishListScreen(
                openWishDetailed = { navigator.navigate(WishDetailedRoute2(wishId = it.id)) },
                onAddWishClicked = { tagId ->
                    navigator.navigate(WishDetailedRoute2(tagId = tagId))
                },
                onSettingIconClicked = { navigator.navigate(SettingsRoute2) },
                onEditTagClick = { navigator.navigate(EditTagRoute2) },
                onGoToBackupScreenClicked = { navigator.navigate(BackupAndRestoreRoute2) },
            )
        }
        entry<WishDetailedRoute2> {
            WishDetailedScreen(
                onBackPressed = popBackStack,
                onWishTagsClicked = { navigator.navigate(WishTagsRoute2(it)) },
                onWishImageClicked = {
                    val route = WishImagesViewerRoute2(
                        wishId = it.wishId,
                        wishImageId = it.wishImageId,
                        wishImageIndex = it.wishImageIndex,
                    )
                    navigator.navigate(route)
                }
            )
        }
        entry<SettingsRoute2> {
            SettingsScreen(
                onBackPressed = popBackStack,
                onAboutAppClicked = { navigator.navigate(AboutAppRoute2) },
                onBackupAndRestoreClicked = { navigator.navigate(BackupAndRestoreRoute2) },
            )
        }
        entry<BackupAndRestoreRoute2> {
            BackupScreen(
                onBackPressed = popBackStack,
                analyticsRepository = analyticsRepository,
            )
        }
        entry<AboutAppRoute2> {
            AboutAppScreen(
                onBackPressed = popBackStack,
                onPrivacyPolicyClicked = { navigator.navigate(PrivacyPolicyRoute2) }
            )
        }
        entry<PrivacyPolicyRoute2> {
            PrivacyPolicyScreen(
                onBackPressed = popBackStack
            )
        }
        entry<WishTagsRoute2> {
            WishTagsScreen(
                onBackPressed = popBackStack
            )
        }
        entry<EditTagRoute2> {
            EditTagsScreen(
                onBackPressed = popBackStack
            )
        }
        entry<WishImagesViewerRoute2> {
            WishImagesViewerScreenRoute(
                onBackPressed = popBackStack
            )
        }
    }

    val animDuration = 400
    val animStiffness = 800f
    val tweenEasing = FastOutSlowInEasing
//    val animSpec = spring<IntOffset>(stiffness = animStiffness)
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