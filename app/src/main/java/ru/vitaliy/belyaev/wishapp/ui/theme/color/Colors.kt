package ru.vitaliy.belyaev.wishapp.ui.theme.color

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

interface ThemeColors {
    val primaryColor: Color
    val backgroundColor: Color
    val backgroundColorSecondary: Color
    val surfaceColor: Color
    val bottomSheetBackgroundColor: Color
    val statusBarColor: Color
    val navigationBarColor: Color
    val iconPrimaryColor: Color
    val errorColor: Color
    val onPrimaryColor: Color
    val onSecondaryColor: Color
    val onSurfaceColor: Color
    val onErrorColor: Color
}

@Stable
data class WishAppDayColors(
    override val primaryColor: Color = DayColors.primaryColor,
    override val backgroundColor: Color = DayColors.backgroundColor,
    override val backgroundColorSecondary: Color = DayColors.backgroundColorSecondary,
    override val surfaceColor: Color = DayColors.surfaceColor,
    override val bottomSheetBackgroundColor: Color = DayColors.bottomSheetBackgroundColor,
    override val statusBarColor: Color = DayColors.statusBarColor,
    override val navigationBarColor: Color = DayColors.navigationBarColor,
    override val iconPrimaryColor: Color = DayColors.iconPrimaryColor,
    override val errorColor: Color = DayColors.errorColor,
    override val onPrimaryColor: Color = DayColors.onPrimaryColor,
    override val onSecondaryColor: Color = DayColors.onSecondaryColor,
    override val onSurfaceColor: Color = DayColors.onSurfaceColor,
    override val onErrorColor: Color = DayColors.onErrorColor,
) : ThemeColors

@Stable
data class WishAppNightColors(
    override val primaryColor: Color = NightColors.primaryColor,
    override val backgroundColor: Color = NightColors.backgroundColor,
    override val backgroundColorSecondary: Color = NightColors.backgroundColorSecondary,
    override val surfaceColor: Color = NightColors.surfaceColor,
    override val bottomSheetBackgroundColor: Color = NightColors.bottomSheetBackgroundColor,
    override val statusBarColor: Color = NightColors.statusBarColor,
    override val navigationBarColor: Color = NightColors.navigationBarColor,
    override val iconPrimaryColor: Color = NightColors.iconPrimaryColor,
    override val errorColor: Color = NightColors.errorColor,
    override val onPrimaryColor: Color = NightColors.onPrimaryColor,
    override val onSecondaryColor: Color = NightColors.onSecondaryColor,
    override val onSurfaceColor: Color = NightColors.onSurfaceColor,
    override val onErrorColor: Color = NightColors.onErrorColor,
) : ThemeColors

