package ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagWithWishCount
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.WishesFilter
import ru.vitaliy.belyaev.wishapp.ui.theme.CommonColors

@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Composable
fun TagsSheetContent(
    tagsWithWishCount: List<TagWithWishCount>,
    wishesFilter: WishesFilter,
    currentWishesCount: Long,
    completedWishesCount: Long,
    onNavItemSelected: (WishesFilter) -> Unit,
    onEditTagsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {

    ConstraintLayout(modifier = modifier.padding(bottom = 8.dp)) {
        val (scrollableRef, staticRef) = createRefs()

        LazyColumn(
            modifier = Modifier.constrainAs(scrollableRef) {
                height = Dimension.preferredWrapContent
                top.linkTo(parent.top)
                bottom.linkTo(staticRef.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            itemsIndexed(tagsWithWishCount) { index, tagWithWishCount ->
                val tag = tagWithWishCount.tag
                val isTagSelected = wishesFilter is WishesFilter.ByTag && wishesFilter.tag.id == tag.id
                NavMenuItemBlock(
                    icon = painterResource(R.drawable.ic_label),
                    title = tag.title,
                    count = tagWithWishCount.wishesCount,
                    isSelected = isTagSelected,
                    bottomCornersRadius = if (index == tagsWithWishCount.lastIndex) 0.dp else 12.dp,
                    onClick = {
                        onNavItemSelected(WishesFilter.ByTag(tag))
                    }
                )
            }
        }
        Column(
            modifier = Modifier.constrainAs(staticRef) {
                height = Dimension.wrapContent
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Divider(color = CommonColors.dividerColor())
            NavMenuItemBlock(
                icon = painterResource(R.drawable.ic_bulleted_list),
                title = stringResource(R.string.all_wishes),
                count = currentWishesCount,
                isSelected = wishesFilter is WishesFilter.All,
                topCornersRadius = 0.dp,
                onClick = {
                    onNavItemSelected(WishesFilter.All)
                }
            )
            NavMenuItemBlock(
                icon = painterResource(R.drawable.ic_check),
                title = stringResource(R.string.completed_wishes),
                count = completedWishesCount,
                isSelected = wishesFilter is WishesFilter.Completed,
                onClick = {
                    onNavItemSelected(WishesFilter.Completed)
                }
            )
            if (tagsWithWishCount.isNotEmpty()) {
                NavMenuItemBlock(
                    icon = painterResource(R.drawable.ic_edit),
                    title = stringResource(R.string.edit_tags),
                    isSelected = false,
                    onClick = onEditTagsClicked
                )
            }
        }
    }
}

@Composable
fun NavMenuItemBlock(
    icon: Painter,
    title: String,
    count: Long = 0,
    isSelected: Boolean,
    topCornersRadius: Dp = 12.dp,
    bottomCornersRadius: Dp = 12.dp,
    onClick: () -> Unit
) {

    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
    val shape = RoundedCornerShape(
        topStart = topCornersRadius,
        bottomStart = bottomCornersRadius,
        topEnd = topCornersRadius,
        bottomEnd = bottomCornersRadius
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = bgColor, shape = shape)
            .clip(shape)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        ThemedIcon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp),
        )

        Spacer(Modifier.weight(1f, true))

        if (count > 0) {
            Text(
                modifier = Modifier.padding(end = 4.dp),
                text = count.toString(),
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
fun NavMenuItemBlockPreview() {
    NavMenuItemBlock(
        icon = painterResource(R.drawable.ic_label),
        title = "Tags",
        count = 1,
        isSelected = false,
        topCornersRadius = 12.dp,
        bottomCornersRadius = 12.dp,
        {}
    )
}