package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp.AboutAppScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp.privacypolicy.PrivacyPolicyScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.EditTagsScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_images.WishImagesViewerScreen
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
    navController: NavHostController,
    onShareClick: (List<WishEntity>) -> Unit,
    analyticsRepository: AnalyticsRepository,
) {
    WishAppAnimatedNavHost(
        navController = navController,
        startDestination = MainRoute.VALUE
    ) {
        composable(MainRoute.VALUE) {
            WishListScreen(
                openWishDetailed = { navController.navigate(WishDetailedRoute.buildRoute(wishId = it.id)) },
                onAddWishClicked = { navController.navigate(WishDetailedRoute.buildRoute()) },
                onSettingIconClicked = { navController.navigate(SettingsRoute.VALUE) },
                onShareClick = onShareClick,
                onEditTagClick = { navController.navigate(EditTagRoute.VALUE) },
                onGoToBackupScreenClicked = { navController.navigate(BackupAndRestoreRoute.VALUE) },
            )
        }
        composable(
            route = WishDetailedRoute.VALUE,
            arguments = listOf(
                navArgument(ARG_WISH_ID) {
                    nullable = true
                    type = NavType.StringType
                },
                navArgument(ARG_WISH_LINK) {
                    nullable = true
                    type = NavType.StringType
                }
            )
        ) {
            WishDetailedScreen(
                onBackPressed = { navController.popBackStack() },
                onWishTagsClicked = { navController.navigate(WishTagsRoute.build(it)) },
                onWishImageClicked = {
                    val route = WishImagesViewerRoute.build(
                        wishId = it.wishId,
                        wishImageId = it.wishImageId,
                        wishImageIndex = it.wishImageIndex,
                    )
                    navController.navigate(route)
                }
            )
        }
        composable(SettingsRoute.VALUE) {
            SettingsScreen(
                onBackPressed = { navController.popBackStack() },
                onAboutAppClicked = { navController.navigate(AboutAppRoute.VALUE) },
                onBackupAndRestoreClicked = { navController.navigate(BackupAndRestoreRoute.VALUE) },
            )
        }
        composable(BackupAndRestoreRoute.VALUE) {
            BackupScreen(
                onBackPressed = { navController.popBackStack() },
                analyticsRepository = analyticsRepository,
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
        composable(
            route = WishImagesViewerRoute.VALUE,
            arguments = listOf(
                navArgument(ARG_WISH_IMAGE_INDEX) {
                    type = NavType.IntType
                }
            )
        ) {
            WishImagesViewerScreenRoute(
                onBackPressed = { navController.popBackStack() }
            )
        }
    }
}