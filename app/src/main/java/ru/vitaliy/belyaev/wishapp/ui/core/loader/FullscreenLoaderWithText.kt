package ru.vitaliy.belyaev.wishapp.ui.core.loader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FullscreenLoaderWithText(
    text: String = "",
    isTranslucent: Boolean = false,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = if (isTranslucent) 0.5f else 1f))
            .clickable {

            }
    ) {
        CircularProgressIndicator()
        if (text.isNotBlank()) {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = text
            )
        }
    }
}