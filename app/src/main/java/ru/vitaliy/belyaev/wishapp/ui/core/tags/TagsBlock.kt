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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsBlock(
    modifier: Modifier = Modifier,
    tags: List<TagEntity>,
    isForList: Boolean = false,
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
        val verticalPadding = 6.dp
        val horizontalPadding = if (isForList) 8.dp else 10.dp
        val textStyle = if (isForList) {
            MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp
            )
        } else {
            MaterialTheme.typography.labelLarge
        }

        repeat(tags.size) {
            Text(
                text = tags[it].title,
                style = textStyle,
                modifier = Modifier
                    .background(color = tagBgColor, shape = shape)
                    .clip(shape)
                    .clickable { onClick() }
                    .padding(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = verticalPadding,
                        bottom = verticalPadding
                    )
            )
        }
    }
}

@Composable
@Preview
private fun TagsBlockPreviewForList() {
    TagsBlock(
        tags = listOf(
            TagEntity("id1", "Tag 1"),
            TagEntity("id2", "Tag 2fjeefjke"),

            ),
        isForList = true,
        onClick = {}
    )
}

@Composable
@Preview
private fun TagsBlockPreviewForWish() {
    TagsBlock(
        tags = listOf(
            TagEntity("id1", "Tag 1"),
            TagEntity("id2", "Tag 2fjeefjke"),

            ),
        isForList = false,
        onClick = {}
    )
}