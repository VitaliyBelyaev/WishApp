package ru.vitaliy.belyaev.wishapp.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(val state: NavigationState) {

    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun goBack(destination: NavKey? = null) {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        // If we're at the base of the current route, go back to the start route stack.
        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
            return
        }

        if (currentRoute == destination) {
            state.topLevelRoute = currentRoute
            return
        }

        val haveDestinationInStack = destination != null && currentStack.any { it == destination }
        if (haveDestinationInStack) {
            while (currentStack.isNotEmpty()) {
                val route = currentStack.lastOrNull()
                if (route == destination) {
                    state.topLevelRoute = route
                    break
                }
                currentStack.removeLastOrNull()
            }
            return
        }

        currentStack.removeLastOrNull()
    }
}