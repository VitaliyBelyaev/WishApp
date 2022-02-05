package ru.vitaliy.belyaev.wishapp.utils

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState

fun ScrollState.isScrollInInitialState(): Boolean = value == 0

fun LazyListState.isScrollInInitialState(): Boolean =
    firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0