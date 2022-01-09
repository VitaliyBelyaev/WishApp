package ru.vitaliy.belyaev.wishapp.ui.screens.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.ui.core.tags.TagsBlock

@Composable
fun WishItemBlock(
    wishItem: WishWithTags,
    isSelected: Boolean,
    onWishClicked: (WishWithTags) -> Unit,
    onWishLongPress: (WishWithTags) -> Unit,
) {
    val backgroundColor: Color = if (isSelected) {
        MaterialTheme.colors.primary.copy(alpha = 0.4f)
    } else {
        Color.Transparent
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
            .border(borderWidth, colorResource(R.color.iconPrimaryColor), baseShape)
            .clip(baseShape)
            .pointerInput(wishItem) {
                detectTapGestures(
                    onTap = { onWishClicked(wishItem) },
                    onLongPress = { onWishLongPress(wishItem) }
                )
            }
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
                style = MaterialTheme.typography.h6,
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
            )
        }
    }
}