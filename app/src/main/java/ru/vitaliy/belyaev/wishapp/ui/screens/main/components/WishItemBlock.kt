package ru.vitaliy.belyaev.wishapp.ui.screens.main.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.tags.TagsBlock
import ru.vitaliy.belyaev.wishapp.ui.theme.localTheme

@ExperimentalFoundationApi
@Composable
fun WishItemBlock(
    wishItem: WishWithTags,
    isSelected: Boolean,
    paddingValues: PaddingValues,
    onWishClicked: (WishWithTags) -> Unit,
    onWishLongPress: (WishWithTags) -> Unit,
//    reorderableListState: ReorderableLazyListState,
    isReorderEnabled: Boolean,
    onMoveItemUp: (WishWithTags) -> Unit,
    onMoveItemDown: (WishWithTags) -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {

        val reorderIconsWidthFraction = 0.2f
        val contentWidthFraction = if (isReorderEnabled) {
            1f - reorderIconsWidthFraction * 2
        } else {
            1f
        }


        if (isReorderEnabled) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth(reorderIconsWidthFraction)
                    .background(color = MaterialTheme.colors.background)
            ) {

                val iconRef = createRef()
                ThemedIcon(
                    painter = painterResource(R.drawable.ic_drag_indicator),
                    contentDescription = "Drag",
                    modifier = Modifier
                        .constrainAs(iconRef) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end, margin = 16.dp)
                        }
                        .requiredSize(36.dp)
                        .clickable {
                            onMoveItemUp(wishItem)
                        }
                )
            }
        }

        Box(modifier = Modifier.padding(paddingValues)) {
            val backgroundColor: Color = if (isSelected) {
                MaterialTheme.colors.primary.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colors.background
            }
            val baseShape = RoundedCornerShape(dimensionResource(R.dimen.base_corner_radius))
            val borderWidth = if (isSelected) {
                1.5.dp
            } else {
                1.dp
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(contentWidthFraction)
                    .background(color = backgroundColor, shape = baseShape)
                    .border(borderWidth, localTheme.colors.iconPrimaryColor, baseShape)
                    .clip(baseShape)
                    .combinedClickable(
                        onLongClick = { onWishLongPress(wishItem) },
                        onClick = { onWishClicked(wishItem) }
                    )
                    .padding(14.dp)
            ) {

                val (title, titleColor) = if (wishItem.title.isNotBlank()) {
                    wishItem.title to Color.Unspecified
                } else {
                    stringResource(R.string.without_title) to Color.Gray
                }
                ConstraintLayout {
                    val (titleRef, linkIconRef) = createRefs()
                    Text(
                        text = title,
                        color = titleColor,
                        style = MaterialTheme.typography.h6.copy(
                            textDecoration = if (wishItem.isCompleted) TextDecoration.LineThrough else null
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.constrainAs(titleRef) {
                            width = Dimension.preferredWrapContent
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            val marginEnd = if (wishItem.link.isNotBlank()) {
                                36.dp
                            } else {
                                0.dp
                            }
                            end.linkTo(parent.end, margin = marginEnd)
                        }
                    )
                    if (wishItem.link.isNotBlank()) {
                        Icon(
                            painterResource(R.drawable.ic_link),
                            contentDescription = "Link",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(20.dp)
                                .constrainAs(linkIconRef) {
                                    start.linkTo(titleRef.end, margin = 8.dp)
                                    end.linkTo(parent.end)
                                    top.linkTo(titleRef.top)
                                    bottom.linkTo(titleRef.bottom)
                                }
                        )
                    }
                }
                if (wishItem.comment.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = wishItem.comment,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (wishItem.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TagsBlock(
                        tags = wishItem.tags,
                        textSize = 13.sp,
                        onClick = { onWishClicked(wishItem) }
                    )
                }
            }
        }


        if (isReorderEnabled) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth(reorderIconsWidthFraction)
                    .background(color = MaterialTheme.colors.background)
            ) {

                val iconRef = createRef()
                ThemedIcon(
                    painter = painterResource(R.drawable.ic_drag_indicator),
                    contentDescription = "Drag",
                    modifier = Modifier
                        .constrainAs(iconRef) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end, margin = 16.dp)
                        }
                        .requiredSize(36.dp)
                        .clickable {
                            onMoveItemDown(wishItem)
                        }
                )
            }
        }
    }
}