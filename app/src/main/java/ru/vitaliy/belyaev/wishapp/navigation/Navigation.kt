package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp.AboutAppScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp.privacypolicy.PrivacyPolicyScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.EditTagsScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.WishListScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed.WishDetailedScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.WishTagsScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun Navigation(
    navController: NavHostController,
    onShareClick: (List<WishEntity>) -> Unit
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
                onEditTagClick = { navController.navigate(EditTagRoute.VALUE) }
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