package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.vitaliy.belyaev.wishapp.ui.main.Main
import ru.vitaliy.belyaev.wishapp.ui.main.MainViewModel
import ru.vitaliy.belyaev.wishapp.ui.wishdetailed.WishDetailed

@Composable
fun Navigation(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = MainScreen.ROUTE) {
        composable(MainScreen.ROUTE) {
            Main(
                { navController.navigate(WishDetailedScreen.route(it.id)) },
                { mainViewModel.onAddWishClicked() },
                {},
                mainViewModel.uiState
            )
        }
        composable(WishDetailedScreen.ROUTE) { navBackStackEntry ->
            WishDetailed(
                { navController.popBackStack() },
                navBackStackEntry.arguments?.getString(WishDetailedScreen.ARG_WISH_ID) ?: ""
            )
        }
    }
}