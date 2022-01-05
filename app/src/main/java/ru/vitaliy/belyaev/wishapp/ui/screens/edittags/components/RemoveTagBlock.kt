package ru.vitaliy.belyaev.wishapp.ui.screens.edittags.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon

@Composable
fun RemoveTagBlock(tag: Tag, onRemoveClick: (Tag) -> Unit) {

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRemoveClick(tag) }
    ) {
        val (iconRef, titleRef) = createRefs()
        val verticalPadding = 16.dp

        ThemedIcon(
            painterResource(R.drawable.ic_delete),
            contentDescription = "Delete",
            modifier = Modifier
                .constrainAs(iconRef) {
                    top.linkTo(parent.top, margin = verticalPadding)
                    bottom.linkTo(parent.bottom, margin = verticalPadding)
                    start.linkTo(parent.start, margin = 16.dp)
                }
        )

        Text(
            text = tag.title,
            modifier = Modifier.constrainAs(titleRef) {
                width = Dimension.fillToConstraints
                start.linkTo(iconRef.end, margin = 16.dp)
                top.linkTo(parent.top, margin = verticalPadding)
                bottom.linkTo(parent.bottom, verticalPadding)
                end.linkTo(parent.end, margin = 16.dp)
            }
        )
    }
}