package ru.vitaliy.belyaev.wishapp.ui.core.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.R

@Composable
fun TagsBlock(
    modifier: Modifier = Modifier,
    tags: List<Tag>,
    textSize: TextUnit,
    onClick: ((Tag) -> Unit)? = null
) {

    val shape = RoundedCornerShape(dimensionResource(R.dimen.base_corner_radius))
    val itemsSpacing = 8.dp
    FlowRow(
        mainAxisSpacing = itemsSpacing,
        crossAxisSpacing = itemsSpacing,
        modifier = modifier
    ) {
        repeat(tags.size) {
            val tag = tags[it]
            val verticalPadding = (textSize.value * 3 / 4).dp
            val horizontalPadding = (textSize.value / 2).dp
            val bgColor: Color = colorResource(R.color.bgSecondary)
            val textModifier = if (onClick != null) {
                Modifier
                    .background(color = bgColor, shape = shape)
                    .clip(shape)
                    .clickable { onClick(tag) }
                    .padding(
                        start = verticalPadding,
                        end = verticalPadding,
                        top = horizontalPadding,
                        bottom = horizontalPadding
                    )
            } else {
                Modifier
                    .background(color = bgColor, shape = shape)
                    .clip(shape)
                    .padding(
                        start = verticalPadding,
                        end = verticalPadding,
                        top = horizontalPadding,
                        bottom = horizontalPadding
                    )
            }
            Text(
                text = tag.title,
                fontSize = textSize,
                modifier = textModifier
            )
        }
    }
}