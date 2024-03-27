package ru.vitaliy.belyaev.wishapp.ui.core.bottomsheet

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

object WishappBottomSheetDefaults {

    // We need call this outside of sheet content, because M3 bottom sheet window consumes insets
    @Composable
    fun navigationBottomPadding(): Dp {
        return with(LocalDensity.current) {
            WindowInsets.navigationBars.getBottom(LocalDensity.current).toDp()
        }
    }
}