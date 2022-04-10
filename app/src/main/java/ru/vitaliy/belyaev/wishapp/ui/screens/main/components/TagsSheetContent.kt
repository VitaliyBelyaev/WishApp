package ru.vitaliy.belyaev.wishapp.ui.screens.main.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.database.Tag
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.TagMenuItem

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Composable
fun TagsSheetContent(
    modalBottomSheetState: ModalBottomSheetState,
    tagMenuItems: List<TagMenuItem>,
    selectedTagId: String,
    onNavItemSelected: (Tag?) -> Unit,
    onEditTagsClicked: () -> Unit
) {

    val scope = rememberCoroutineScope()

    BackHandler(enabled = modalBottomSheetState.isVisible) {
        scope.launch { modalBottomSheetState.hide() }
    }

    ConstraintLayout {
        val (closeButtonRef, scrollableRef, staticRef) = createRefs()

        IconButton(
            onClick = { scope.launch { modalBottomSheetState.hide() } },
            modifier = Modifier
                .constrainAs(closeButtonRef) {
                    top.linkTo(parent.top, margin = 8.dp)
                    end.linkTo(parent.end, margin = 8.dp)
                }
        ) {
            ThemedIcon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.constrainAs(scrollableRef) {
                height = Dimension.preferredWrapContent
                top.linkTo(closeButtonRef.bottom)
                bottom.linkTo(staticRef.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            items(tagMenuItems) { navMenuItem ->
                NavMenuItemBlock(
                    icon = painterResource(R.drawable.ic_label),
                    title = navMenuItem.tag.title,
                    isSelected = navMenuItem.isSelected,
                    onClick = {
                        onNavItemSelected(navMenuItem.tag)
                        scope.launch { modalBottomSheetState.snapTo(ModalBottomSheetValue.Hidden) }
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
            Divider()
            NavMenuItemBlock(
                icon = painterResource(R.drawable.ic_list_bulleted),
                title = stringResource(R.string.all_wishes),
                isSelected = selectedTagId.isBlank(),
                onClick = {
                    onNavItemSelected(null)
                    scope.launch { modalBottomSheetState.snapTo(ModalBottomSheetValue.Hidden) }
                }
            )
            NavMenuItemBlock(
                icon = painterResource(R.drawable.ic_edit),
                title = stringResource(R.string.edit_tags),
                isSelected = false,
                onClick = {
                    scope.launch {
                        modalBottomSheetState.snapTo(ModalBottomSheetValue.Hidden)
                        onEditTagsClicked()
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun NavMenuItemBlock(
    icon: Painter,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    val bgColor = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.3f) else Color.Transparent
    val cornerRadius = 50.dp
    val shape = RoundedCornerShape(topEnd = cornerRadius, bottomEnd = cornerRadius)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(end = 16.dp)
            .background(color = bgColor, shape = shape)
            .padding(16.dp)
    ) {
        ThemedIcon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Preview
@Composable
fun NavMenuItemBlockPreview() {
    NavMenuItemBlock(
        painterResource(R.drawable.ic_label),
        "Tags",
        false,
        {}
    )
}