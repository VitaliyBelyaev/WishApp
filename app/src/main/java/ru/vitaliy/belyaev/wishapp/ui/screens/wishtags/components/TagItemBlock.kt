package ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.entity.TagItem

@Composable
fun TagItemBlock(tagItem: TagItem, onClick: (TagItem) -> Unit) {

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(tagItem) }
    ) {
        val (iconRef, titleRef, checkboxRef) = createRefs()
        val verticalPadding = 8.dp

        ThemedIcon(
            painter = painterResource(R.drawable.ic_label),
            contentDescription = "Tag",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .constrainAs(iconRef) {
                    top.linkTo(parent.top, margin = verticalPadding)
                    bottom.linkTo(parent.bottom, margin = verticalPadding)
                    start.linkTo(parent.start, margin = 16.dp)
                }
        )
        Text(
            text = tagItem.tag.title,
            modifier = Modifier.constrainAs(titleRef) {
                width = Dimension.fillToConstraints
                start.linkTo(iconRef.end, margin = 16.dp)
                top.linkTo(parent.top, margin = verticalPadding)
                bottom.linkTo(parent.bottom, verticalPadding)
                end.linkTo(checkboxRef.start, margin = 8.dp)
            }
        )
        val checkboxColors = CheckboxDefaults.colors()

        Checkbox(
            checked = tagItem.isSelected,
            onCheckedChange = { onClick(tagItem) },
            colors = checkboxColors,
            modifier = Modifier.constrainAs(checkboxRef) {
                top.linkTo(parent.top, margin = verticalPadding)
                bottom.linkTo(parent.bottom, margin = verticalPadding)
                end.linkTo(parent.end, margin = 16.dp)
            }
        )

    }
}

@Composable
@Preview
fun TagItemBlockPreview() {
    TagItemBlock(TagItem(TagEntity("1", "Др"), false), {})
}