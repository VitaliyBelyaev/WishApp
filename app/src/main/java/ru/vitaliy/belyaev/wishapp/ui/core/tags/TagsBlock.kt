package ru.vitaliy.belyaev.wishapp.ui.core.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.database.Tag
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.theme.localTheme

@Composable
fun TagsBlock(
    modifier: Modifier = Modifier,
    tags: List<Tag>,
    textSize: TextUnit,
    onClick: () -> Unit,
    onAddNewTagClick: (() -> Unit)? = null
) {
    val itemsSpacing = 8.dp
    FlowRow(
        mainAxisSpacing = itemsSpacing,
        crossAxisSpacing = itemsSpacing,
        modifier = modifier
    ) {
        val shape = RoundedCornerShape(dimensionResource(R.dimen.base_corner_radius))
        val tagBgColor: Color = localTheme.colors.backgroundColorSecondary
        val verticalPadding = (textSize.value * 3 / 4).dp
        val horizontalPadding = (textSize.value / 2).dp

        if (onAddNewTagClick != null) {
            val borderWidth = 1.dp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(
                        width = borderWidth,
                        color = MaterialTheme.colors.primary,
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
                    modifier = Modifier
                        .padding(end = 4.dp)
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