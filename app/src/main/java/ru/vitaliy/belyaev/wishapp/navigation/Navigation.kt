package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.IntOffset
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlin.math.roundToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp.AboutAppScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp.privacypolicy.PrivacyPolicyScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.EditTagsScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.main.MainScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed.WishDetailedScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.WishTagsScreen
import timber.log.Timber

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun Navigation(onShareClick: (List<WishWithTags>) -> Unit) {
    val navController = rememberAnimatedNavController()
    val dampingRatio: Float = Spring.DampingRatioNoBouncy
    val stiffness: Float = 150f
    val visibilityThreshold: IntOffset = IntOffset(1, 1)
    val visibilityThreshold2: IntOffset = IntOffset(200, 1)
    AnimatedNavHost(
        navController = navController,
        startDestination = MainRoute.VALUE,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentScope.SlideDirection.Left,
                animationSpec = spring(
                    dampingRatio = dampingRatio,
                    stiffness = stiffness,
                    visibilityThreshold = visibilityThreshold
                )
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentScope.SlideDirection.Left,
                animationSpec = spring(
                    dampingRatio = dampingRatio,
                    stiffness = stiffness,
                    visibilityThreshold = visibilityThreshold
                ),
                targetOffset = {
                    Timber.tag("RTRT").d("targetOffset:$it")
                    (it * 0.65).roundToInt()
                }
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentScope.SlideDirection.Right,
                animationSpec = spring(
                    dampingRatio = dampingRatio,
                    stiffness = stiffness,
                    visibilityThreshold = visibilityThreshold
                ),
                initialOffset = {
                    (it * 0.65).roundToInt()
                }
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentScope.SlideDirection.Right,
                animationSpec = spring(
                    dampingRatio = dampingRatio,
                    stiffness = stiffness,
                    visibilityThreshold = visibilityThreshold
                )
            )
        }
    ) {
        composable(MainRoute.VALUE) {
            MainScreen(
                openWishDetailed = { navController.navigate(WishDetailedRouteWithArgs.build(it.id)) },
                onAddWishClicked = { navController.navigate(WishDetailedRoute.VALUE) },
                onSettingIconClicked = { navController.navigate(SettingsRoute.VALUE) },
                onShareClick = onShareClick,
                onEditTagClick = { navController.navigate(EditTagRoute.VALUE) }
            )
        }
        composable(WishDetailedRoute.VALUE) {
            WishDetailedScreen(
                onBackPressed = { navController.popBackStack() },
                onWishTagsClicked = { navController.navigate(WishTagsRoute.build(it)) }
            )
        }
        composable(WishDetailedRouteWithArgs.VALUE) {
            WishDetailedScreen(
                onBackPressed = { navController.popBackStack() },
                onWishTagsClicked = { navController.navigate(WishTagsRoute.build(it)) }
            )
        }
        composable(SettingsRoute.VALUE) {
            SettingsScreen(
                onBackPressed = { navController.popBackStack() },
                onAboutAppClicked = { navController.navigate(AboutAppRoute.VALUE) }
            )
        }
        composable(AboutAppRoute.VALUE) {
            AboutAppScreen(
                onBackPressed = { navController.popBackStack() },
                onPrivacyPolicyClicked = { navController.navigate(PrivacyPolicyRoute.VALUE) }
            )
        }
        composable(PrivacyPolicyRoute.VALUE) {
            PrivacyPolicyScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable(WishTagsRoute.VALUE) {
            WishTagsScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable(EditTagRoute.VALUE) {
            EditTagsScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
    }
}