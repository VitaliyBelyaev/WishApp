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
import androidx.compose.foundation.layout.sizeIn
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
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.Progress

@Composable
fun FullscreenLoaderWithText(
    modifier: Modifier = Modifier,
    text: String = "",
    isTranslucent: Boolean = false,
    isLinear: Boolean = true,
    progress: Progress? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val loaderWidth = 8.dp
    val horizontalPadding = 32.dp
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
                strokeWidth = loaderWidth,
                strokeCap = StrokeCap.Round,
            )
        }

        if (text.isNotBlank()) {
            Text(
                modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                text = text
            )
        }

        if (isLinear) {
            val sizeModifier = Modifier
                .height(loaderWidth)
                .sizeIn(maxWidth = 500.dp)
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
            if (progress != null) {
                LinearProgressIndicator(
                    progress = progress.progress.toFloat(),
                    modifier = sizeModifier,
                    strokeCap = StrokeCap.Round,
                )

                Text(
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End,
                    text = progress.progressCurrentToTotalString
                )
            } else {
                LinearProgressIndicator(
                    modifier = sizeModifier,
                    strokeCap = StrokeCap.Round,
                )
            }
        }
    }
}