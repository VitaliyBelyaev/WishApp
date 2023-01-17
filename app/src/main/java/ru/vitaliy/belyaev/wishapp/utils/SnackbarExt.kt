package ru.vitaliy.belyaev.wishapp.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState

suspend fun SnackbarHostState.showDismissableSnackbar(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration =
        if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
) {
    showSnackbar(
        message = message,
        actionLabel = actionLabel,
        withDismissAction = true,
        duration = duration
    )
}