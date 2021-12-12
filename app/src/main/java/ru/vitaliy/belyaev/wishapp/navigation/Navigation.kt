package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp.AboutAppScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp.privacypolicy.PrivacyPolicyScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.main.MainScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed.WishDetailedScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.WishTagsScreen

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun Navigation(onShareClick: (List<WishWithTags>) -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = MainRoute.VALUE) {
        composable(MainRoute.VALUE) {
            MainScreen(
                openWishDetailed = { navController.navigate(WishDetailedRouteWithArgs.build(it.id)) },
                onAddWishClicked = { navController.navigate(WishDetailedRoute.VALUE) },
                onSettingIconClicked = { navController.navigate(SettingsRoute.VALUE) },
                onShareClick = onShareClick
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
    }
}