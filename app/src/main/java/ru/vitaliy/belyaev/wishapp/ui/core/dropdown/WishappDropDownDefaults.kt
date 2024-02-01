package ru.vitaliy.belyaev.wishapp.ui.core.dropdown

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object WishappDropDownDefaults {

    @Composable
    fun dropDownMenuItemMinWidth(): Dp {
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        return screenWidth / 2
    }
}

