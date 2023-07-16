package ru.vitaliy.belyaev.wishapp.utils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@SuppressLint("ComposableNaming")
@Composable
fun trackScreenShow(
    trackCallback: () -> Unit,
) {
    DisposableEffect(Unit) {
        onDispose {
            // This block will be called when the effect is disposed
            trackCallback.invoke()
        }
    }
}