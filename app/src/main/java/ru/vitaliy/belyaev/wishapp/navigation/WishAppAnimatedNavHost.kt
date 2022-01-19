package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import kotlin.math.roundToInt

@ExperimentalAnimationApi
@Composable
fun WishAppAnimatedNavHost(
    navController: NavHostController,
    startDestination: String,
    builder: NavGraphBuilder.() -> Unit
) {
    val dampingRatio: Float = Spring.DampingRatioNoBouncy
    val stiffness: Float = 400f
    val visibilityThreshold: IntOffset = IntOffset.VisibilityThreshold

    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
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
                )
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
        },
        builder = builder
    )
}