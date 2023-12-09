package ru.vitaliy.belyaev.wishapp.ui.core.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsBlock(
    modifier: Modifier = Modifier,
    tags: List<TagEntity>,
    textSize: TextUnit,
    onClick: () -> Unit,
    onAddNewTagClick: (() -> Unit)? = null
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

        if (onAddNewTagClick != null) {
            val borderWidth = 1.dp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(
                        width = borderWidth,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        shape = shape
                    )
                    .clip(shape)
                    .clickable { onAddNewTagClick() }
                    .padding(
                        start = verticalPadding - borderWidth,
                        end = verticalPadding - borderWidth,
                        top = horizontalPadding - borderWidth,
                        bottom = horizontalPadding - borderWidth
                    )
            ) {
                ThemedIcon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = "Add label",
                    modifier = Modifier.padding(end = 4.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.add_tag),
                    fontSize = textSize
                )
            }
        }
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