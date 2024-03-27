package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import kotlin.math.roundToInt

object NavTransitions {

    object None {

        val enter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
            EnterTransition.None
        }

        val exit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
            ExitTransition.None
        }

        val popEnter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
            EnterTransition.None
        }

        val popExit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
            ExitTransition.None
        }
    }

    object SlideInOut {

        private const val dampingRatio: Float = Spring.DampingRatioNoBouncy
        private const val stiffness = 600f
        private val visibilityThreshold: IntOffset = IntOffset.VisibilityThreshold

        // Оставил красивые анимации похожие на те что в телеге, не стал юзать пока, так как
        // на девайсе жутко тормозило при возвращении на "тяжелые экраны", надо будет потестить
        val enter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = spring(
                    dampingRatio = dampingRatio,
                    stiffness = stiffness,
                    visibilityThreshold = visibilityThreshold
                )
            )
        }

        val exit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = spring(
                    dampingRatio = dampingRatio,
                    stiffness = stiffness,
                    visibilityThreshold = visibilityThreshold
                ),
                targetOffset = {
                    (it * 0.3f).roundToInt()
                }
            )
        }

        val popEnter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = spring(
                    dampingRatio = dampingRatio,
                    stiffness = stiffness,
                    visibilityThreshold = visibilityThreshold
                ),
                initialOffset = {
                    (it * 0.3f).roundToInt()
                }
            )
        }

        val popExit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = spring(
                    dampingRatio = dampingRatio,
                    stiffness = stiffness,
                    visibilityThreshold = visibilityThreshold
                )
            )
        }
    }
}