package ru.vitaliy.belyaev.wishapp.ui.core.bottomsheet

import androidx.compose.ui.unit.dp

object WishappBottomSheetDefaults{


    // Uses for M3 bottom sheet content bottom padding, because default windowInsets for M3 bottom sheet
    // with nav bar inset is ugly and animation is not smooth. I try make bottom padding with inset
    // like Modifier.windowInsetsPadding(WindowInsets.navigationBars) but it doesn't work on emulator and
    // Samsung, but just raw padding works fine. Only downside is that it hardcoded as height of biggest 3 button
    // navbar and on gesture nav bars it looks big.
    val bottomPadding = 48.dp
}