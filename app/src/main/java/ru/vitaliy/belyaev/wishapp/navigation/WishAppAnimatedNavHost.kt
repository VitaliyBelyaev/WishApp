package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
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
    val dampingRatio: Float = Spring.DampingRatioNoBouncy
    val stiffness = 1750f
    val visibilityThreshold: IntOffset = IntOffset.VisibilityThreshold

//    // Оставил красивые анимации похожие на те что в телеге, не стал юзать пока, так как
//    // на девайсе жутко тормозило при возвращении на "тяжелые экраны", надо будет потестить
//    val enter: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition) = {
//        slideIntoContainer(
//            towards = AnimatedContentScope.SlideDirection.Left,
//            animationSpec = spring(
//                dampingRatio = dampingRatio,
//                stiffness = stiffness,
//                visibilityThreshold = visibilityThreshold
//            )
//        )
//    }
//
//    val exit: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition) = {
//        slideOutOfContainer(
//            towards = AnimatedContentScope.SlideDirection.Left,
//            animationSpec = spring(
//                dampingRatio = dampingRatio,
//                stiffness = stiffness,
//                visibilityThreshold = visibilityThreshold
//            ),
//            targetOffset = {
//                (it * 0.1f).roundToInt()
//            }
//        )
//    }
//
//    val popEnter: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition) = {
//        slideIntoContainer(
//            towards = AnimatedContentScope.SlideDirection.Right,
//            animationSpec = spring(
//                dampingRatio = dampingRatio,
//                stiffness = stiffness,
//                visibilityThreshold = visibilityThreshold
//            ),
//            initialOffset = {
//                (it * 0.1f).roundToInt()
//            }
//        )
//    }
//
//    val popExit: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition) = {
//        slideOutOfContainer(
//            towards = AnimatedContentScope.SlideDirection.Right,
//            animationSpec = spring(
//                dampingRatio = dampingRatio,
//                stiffness = stiffness,
//                visibilityThreshold = visibilityThreshold
//            )
//        )
//    }

    // Вырубил анимации просто, так как обычный NavHost тупо юзает
    // Crossfade(androidx.navigation:navigation-compose:2.5.3), которым вообще никак
    // нельзя управлять и если у приложения тема отличная от темы девайса или это девайс на
    // пре 10 андроиде, то этот кроссфейд видно как мигание.
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        builder = builder
    )
}