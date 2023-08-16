package ru.vitaliy.belyaev.wishapp.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object WishAppButtonColors {

    @Composable
    fun destructiveTextButtonColors(
        containerColor: Color = Color.Transparent,
        contentColor: Color = MaterialTheme.colorScheme.error,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = MaterialTheme.colorScheme.error.copy(alpha = 0.38f),
    ): ButtonColors = ButtonDefaults.textButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )

    @Composable
    fun destructiveFilledTonalButtonColors(
        containerColor: Color = MaterialTheme.colorScheme.errorContainer,
        contentColor: Color = MaterialTheme.colorScheme.onErrorContainer,
        disabledContainerColor: Color = MaterialTheme.colorScheme.errorContainer
            .copy(alpha = 0.12f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onErrorContainer
            .copy(alpha = 0.38f),
    ): ButtonColors = ButtonDefaults.filledTonalButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
}