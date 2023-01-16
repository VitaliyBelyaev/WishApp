package ru.vitaliy.belyaev.wishapp.ui.theme.material3

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object CommonColors {

    @Composable
    fun bottomSheetBgColor(): Color {
        return MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    }

    @Composable
    fun navBarColor(colorScheme: ColorScheme? = null): Color {
        return colorScheme?.background ?: MaterialTheme.colorScheme.background
    }
}