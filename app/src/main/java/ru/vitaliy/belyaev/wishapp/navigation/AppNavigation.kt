package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import ru.vitaliy.belyaev.wishapp.ui.screens.lookpreview.LookPreviewScreen

/**
 * Навигация нового вида. Прежний вид живёт в `navigation/old` со своим стеком,
 * при смене вида стек начинается заново со списка.
 */
@Serializable
data object LookPreviewRoute : NavKey

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(LookPreviewRoute)
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            // Временная витрина темы и компонентов этапа 1. Каркас списка заменит её в части 2.2
            entry<LookPreviewRoute> { LookPreviewScreen() }
        },
    )
}
