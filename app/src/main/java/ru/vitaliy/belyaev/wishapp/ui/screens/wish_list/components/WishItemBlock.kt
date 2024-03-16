package ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.tags.TagsBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.MoveDirection
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.ReorderButtonState

@ExperimentalFoundationApi
@Composable
fun WishItemBlock(
    wishItem: WishEntity,
    isSelected: Boolean,
    horizontalOuterPadding: Dp,
    onWishClicked: (WishEntity) -> Unit,
    onWishLongPress: (WishEntity) -> Unit,
    reorderButtonState: ReorderButtonState,
    onMoveItem: (WishEntity, MoveDirection) -> Unit,
    modifier: Modifier = Modifier
) {

    val isReorderEnabled = reorderButtonState is ReorderButtonState.Visible &&
            reorderButtonState.isEnabled

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (isReorderEnabled) 0.dp else horizontalOuterPadding),
    ) {

        if (isReorderEnabled) {
            IconButton(
                onClick = { onMoveItem(wishItem, MoveDirection.UP) },
                modifier = Modifier.weight(2f)
            ) {

                ThemedIcon(
                    modifier = Modifier.requiredSize(36.dp),
                    painter = painterResource(R.drawable.ic_arrow_up),
                    contentDescription = "Move up",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(modifier = Modifier.weight(8f)) {
            val backgroundColor: Color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            } else {
                Color.Transparent
            }
            val baseShape = MaterialTheme.shapes.medium
            val borderWidth = if (isSelected) {
                2.dp
            } else {
                1.dp
            }
            val borderColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
            }

            val horizontalInnerPadding = 14.dp
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = backgroundColor, shape = baseShape)
                    .border(borderWidth, borderColor, baseShape)
                    .clip(baseShape)
                    .combinedClickable(
                        onLongClick = { onWishLongPress(wishItem) },
                        onClick = { onWishClicked(wishItem) }
                    )
                    .padding(vertical = 14.dp)
            ) {

                val (title, titleColor) = if (wishItem.title.isNotBlank()) {
                    wishItem.title to MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    stringResource(R.string.without_title) to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }

                Text(
                    text = title,
                    color = titleColor,
                    style = MaterialTheme.typography.titleLarge.copy(
                        textDecoration = if (wishItem.isCompleted) TextDecoration.LineThrough else null,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalInnerPadding),
                )

                if (wishItem.comment.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = wishItem.comment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalInnerPadding),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (wishItem.link.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = horizontalInnerPadding),
                    ) {
                        ThemedIcon(
                            painterResource(R.drawable.ic_link),
                            contentDescription = "Link",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${wishItem.links.size}",
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }

                if (wishItem.images.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val itemsSpacing = 8.dp
                    val imageHeight = 130.dp

                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageHeight)
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        wishItem.images.forEachIndexed { index, image ->
                            val imageRequest = ImageRequest.Builder(LocalContext.current)
                                .data(image.rawData)
                                .memoryCacheKey(image.id)
                                .crossfade(true)
                                .build()


                            val paddingStart = if (index == 0) horizontalInnerPadding else 0.dp
                            val paddingEnd = if (index == wishItem.images.lastIndex) horizontalInnerPadding else 0.dp

                            val maxWidth = LocalConfiguration.current.screenWidthDp.dp -
                                    horizontalInnerPadding -
                                    itemsSpacing -
                                    horizontalOuterPadding
                            AsyncImage(
                                model = imageRequest,
                                contentDescription = null,
                                modifier = Modifier
                                    .height(imageHeight)
                                    .sizeIn(maxWidth = maxWidth)
                                    .padding(start = paddingStart, end = paddingEnd)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { onWishClicked(wishItem) },
                                contentScale = ContentScale.FillHeight
                            )
                        }
                    }
                }

                if (wishItem.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TagsBlock(
                        modifier = Modifier
                            .padding(horizontal = horizontalInnerPadding),
                        tags = wishItem.tags,
                        isForList = true,
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
                    modifier = Modifier.requiredSize(36.dp),
                    painter = painterResource(R.drawable.ic_arrow_down),
                    contentDescription = "Move down",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}