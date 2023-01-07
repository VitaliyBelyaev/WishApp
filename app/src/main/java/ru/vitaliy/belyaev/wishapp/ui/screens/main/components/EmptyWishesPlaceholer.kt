package ru.vitaliy.belyaev.wishapp.ui.screens.main.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.ui.theme.localTheme

@Composable
fun EmptyWishesPlaceholder(
    text: String,
    modifier: Modifier = Modifier
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(start = 32.dp, end = 32.dp)
    ) {
        Text(
            text = text,
            color = localTheme.colors.secondaryTextColor,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )

    }
}
