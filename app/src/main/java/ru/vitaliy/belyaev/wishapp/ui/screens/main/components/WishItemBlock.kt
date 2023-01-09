package ru.vitaliy.belyaev.wishapp.ui.screens.main.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.tags.TagsBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.MoveDirection
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.ReorderButtonState
import ru.vitaliy.belyaev.wishapp.ui.theme.localTheme

@ExperimentalFoundationApi
@Composable
fun WishItemBlock(
    wishItem: WishWithTags,
    isSelected: Boolean,
    horizontalPadding: Dp,
    onWishClicked: (WishWithTags) -> Unit,
    onWishLongPress: (WishWithTags) -> Unit,
    reorderButtonState: ReorderButtonState,
    onMoveItem: (WishWithTags, MoveDirection) -> Unit,
    modifier: Modifier = Modifier
) {

    val isReorderEnabled = reorderButtonState is ReorderButtonState.Visible &&
            reorderButtonState.isEnabled

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (isReorderEnabled) 0.dp else horizontalPadding),
    ) {

        if (isReorderEnabled) {
            IconButton(
                onClick = { onMoveItem(wishItem, MoveDirection.UP) },
                modifier = Modifier.weight(2f)
            ) {

                ThemedIcon(
                    painter = painterResource(R.drawable.ic_arrow_up_24),
                    contentDescription = "Move up",
                    modifier = Modifier
                        .requiredSize(36.dp)

                )
            }
        }

        Box(modifier = Modifier.weight(8f)) {
            val backgroundColor: Color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.background
            }
            val baseShape = RoundedCornerShape(dimensionResource(R.dimen.base_corner_radius))
            val borderWidth = if (isSelected) {
                1.5.dp
            } else {
                1.dp
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                        style = MaterialTheme.typography.titleLarge.copy(
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
                        ThemedIcon(
                            painterResource(R.drawable.ic_link),
                            contentDescription = "Link",
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
            IconButton(
                onClick = { onMoveItem(wishItem, MoveDirection.DOWN) },
                modifier = Modifier.weight(2f)
            ) {
                ThemedIcon(
                    painter = painterResource(R.drawable.ic_arrow_down_24),
                    contentDescription = "Move down",
                    modifier = Modifier
                        .requiredSize(36.dp)
                )
            }
        }
    }
}