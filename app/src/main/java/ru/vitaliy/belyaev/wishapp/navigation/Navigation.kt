package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.vitaliy.belyaev.wishapp.ui.screens.main.MainScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.modifywish.ModifyWishScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed.WishDetailedScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = MainRoute.VALUE) {
        composable(MainRoute.VALUE) {
            MainScreen(
                onWishClicked = { navController.navigate(ModifyWishRouteWithArgs.build(it.id)) },
                onAddWishClicked = { navController.navigate(ModifyWishRoute.VALUE) },
                onSettingIconClicked = { navController.navigate(SettingsRoute.VALUE) }
            )
        }
        composable(SettingsRoute.VALUE) {
            SettingsScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable(WishDetailedRoute.VALUE) { navBackStackEntry ->
            WishDetailedScreen(
                onBackPressed = { navController.popBackStack() },
                wishId = navBackStackEntry.arguments?.getString(WishDetailedRoute.ARG_WISH_ID) ?: ""
            )
        }
        composable(ModifyWishRoute.VALUE) {
            ModifyWishScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable(ModifyWishRouteWithArgs.VALUE) {
            ModifyWishScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
    }
}