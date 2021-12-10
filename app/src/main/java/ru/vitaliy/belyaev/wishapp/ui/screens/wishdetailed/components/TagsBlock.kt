package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.R

@Composable
fun TagsBlock(tags: List<Tag>, onClick: (Tag) -> Unit, modifier: Modifier = Modifier) {

    val shape = RoundedCornerShape(dimensionResource(R.dimen.base_corner_radius))
    val itemsSpacing = 8.dp
    FlowRow(
        mainAxisSpacing = itemsSpacing,
        crossAxisSpacing = itemsSpacing,
        modifier = modifier
    ) {
        repeat(tags.size) {
            val tag = tags[it]
            val verticalPadding = 12.dp
            val horizontalPadding = 8.dp
            Text(
                text = tag.title,
                modifier = Modifier
                    .background(color = colorResource(R.color.bgSecondary), shape = shape)
                    .clip(shape)
                    .clickable { onClick(tag) }
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