package ru.vitaliy.belyaev.wishapp.ui.core.loader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun FullscreenLoaderWithText(
    modifier: Modifier = Modifier,
    text: String = "",
    isTranslucent: Boolean = false,
    isLinear: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = if (isTranslucent) 0.95f else 1f))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
            }
    ) {
        if (!isLinear) {
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                strokeWidth = 6.dp,
                strokeCap = StrokeCap.Round,
            )
        }

        if (text.isNotBlank()) {
            Text(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                text = text
            )
        }

        if (isLinear) {
            LinearProgressIndicator(
                modifier = Modifier
                    .height(6.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                strokeCap = StrokeCap.Round,
            )
        }
    }
}