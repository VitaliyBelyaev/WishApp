package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@ExperimentalAnimationApi
@Composable
fun WishAppAnimatedNavHost(
    navController: NavHostController,
    startDestination: String,
    builder: NavGraphBuilder.() -> Unit
) {
    // Вырубил анимации просто, так как обычный NavHost тупо юзает
    // Crossfade(androidx.navigation:navigation-compose:2.5.3), которым вообще никак
    // нельзя управлять и если у приложения тема отличная от темы девайса или это девайс на
    // пре 10 андроиде, то этот кроссфейд видно как мигание.
    // Если с fadeInOut, то мигание явно заметно, с аля Телега slideInOut - не так явно, но иногда
    // проскальзывает, при этом если тема как системная, то все ок.
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = NavTransitions.None.enter,
        exitTransition = NavTransitions.None.exit,
        popEnterTransition = NavTransitions.None.popEnter,
        popExitTransition = NavTransitions.None.popExit,
        builder = builder
    )
}