package ru.vitaliy.belyaev.wishapp.ui.core.navbarcolor

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun DefaultNavbarColor() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colors.isLight
    val navbarColor = MaterialTheme.colors.background
    SideEffect {
        systemUiController.setNavigationBarColor(
            color = navbarColor,
            darkIcons = useDarkIcons
        )
    }
}