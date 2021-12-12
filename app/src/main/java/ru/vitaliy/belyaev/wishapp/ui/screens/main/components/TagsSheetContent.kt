package ru.vitaliy.belyaev.wishapp.ui.screens.main.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.AllTagsMenuItem
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.NavigationMenuItem
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.TagMenuItem

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Composable
fun TagsSheetContent(
    modalBottomSheetState: ModalBottomSheetState,
    navMenuItems: List<NavigationMenuItem>,
    onNavItemSelected: (Tag?) -> Unit,
    onEditTagsClicked: () -> Unit
) {

    val scope = rememberCoroutineScope()

    BackHandler(enabled = modalBottomSheetState.isVisible) {
        scope.launch { modalBottomSheetState.hide() }
    }

    LazyColumn {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            NavMenuItemBlock(
                icon = painterResource(R.drawable.ic_edit),
                title = stringResource(R.string.edit_tags),
                isSelected = false,
                onClick = {
                    scope.launch {
                        modalBottomSheetState.hide()
                        onEditTagsClicked()
                    }
                }
            )
        }
        item { Divider() }

        items(navMenuItems) { navMenuItem ->
            val icon = when (navMenuItem) {
                is AllTagsMenuItem -> painterResource(R.drawable.ic_list_bulleted)
                is TagMenuItem -> painterResource(R.drawable.ic_label)
            }
            val title = when (navMenuItem) {
                is AllTagsMenuItem -> stringResource(navMenuItem.titleRes)
                is TagMenuItem -> navMenuItem.tag.title
            }

            NavMenuItemBlock(
                icon = icon,
                title = title,
                isSelected = navMenuItem.isSelected,
                onClick = {
                    val tag = when (navMenuItem) {
                        is AllTagsMenuItem -> null
                        is TagMenuItem -> navMenuItem.tag
                    }
                    onNavItemSelected(tag)
                    scope.launch { modalBottomSheetState.hide() }
                }
            )
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

    val bgColor = if (isSelected) colorResource(R.color.bgSecondary) else Color.Transparent
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
        Icon(
            painter = icon,
            contentDescription = null,
            tint = Color.Gray,
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