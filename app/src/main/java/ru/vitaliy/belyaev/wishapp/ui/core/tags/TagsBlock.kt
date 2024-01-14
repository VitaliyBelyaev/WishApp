package ru.vitaliy.belyaev.wishapp.ui.core.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsBlock(
    modifier: Modifier = Modifier,
    tags: List<TagEntity>,
    textSize: TextUnit,
    onClick: () -> Unit,
) {
    val itemsSpacing = 8.dp
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
        verticalArrangement = Arrangement.spacedBy(itemsSpacing),
        modifier = modifier
    ) {
        val shape = MaterialTheme.shapes.small
        val tagBgColor: Color = MaterialTheme.colorScheme.surfaceVariant
        val verticalPadding = (textSize.value * 3 / 4).dp
        val horizontalPadding = (textSize.value / 2).dp

        repeat(tags.size) {
            Text(
                text = tags[it].title,
                fontSize = textSize,
                modifier = Modifier
                    .background(color = tagBgColor, shape = shape)
                    .clip(shape)
                    .clickable { onClick() }
                    .padding(
                        start = verticalPadding,
                        end = verticalPadding,
                        top = horizontalPadding,
                        bottom = horizontalPadding
                    )
            )
        }
    }
}