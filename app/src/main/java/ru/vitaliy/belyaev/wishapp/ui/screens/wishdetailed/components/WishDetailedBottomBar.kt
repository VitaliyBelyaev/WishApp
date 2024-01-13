package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.WishItem

@Composable
fun WishDetailedBottomBar(
    wishItem: WishItem?,
    onWishTagsClicked: (String) -> Unit,
    onAddImageClicked: () -> Unit,
    onWishCompletedClicked: (String, Boolean) -> Unit,
) {

    BottomAppBar(
        contentPadding = PaddingValues(),
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .height(56.dp),
        actions = {
            Actions(
                wishItem = wishItem,
                onWishTagsClicked = onWishTagsClicked,
                onAddImageClicked = onAddImageClicked,
                onWishCompletedClicked = onWishCompletedClicked,
            )
        },
    )
}

@Composable
private fun RowScope.Actions(
    wishItem: WishItem?,
    onWishTagsClicked: (String) -> Unit,
    onAddImageClicked: () -> Unit,
    onWishCompletedClicked: (String, Boolean) -> Unit,
) {

    IconButton(onClick = onAddImageClicked) {
        ThemedIcon(
            painterResource(R.drawable.ic_add_image),
            contentDescription = "Add image",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    IconButton(
        onClick = {
            val wishId = wishItem?.wish?.id ?: return@IconButton
            onWishTagsClicked(wishId)
        }
    ) {
        Icon(
            painterResource(R.drawable.ic_label),
            contentDescription = "Open tags",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(Modifier.weight(1f, true))

    wishItem?.let {
        val isCompleted = it.wish.isCompleted
        val text = if (isCompleted) {
            stringResource(R.string.wish_not_done)
        } else {
            stringResource(R.string.wish_done)
        }

        TextButton(
            onClick = { onWishCompletedClicked(it.wish.id, isCompleted) },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun CustomBottomAppBar(
    modifier: Modifier = Modifier,
    containerColor: Color = BottomAppBarDefaults.containerColor,
    tonalElevation: Dp = BottomAppBarDefaults.ContainerElevation,
    windowInsets: WindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
    actions: @Composable RowScope.() -> Unit
) {
    Surface(
        color = BottomAppBarDefaults.containerColor,
        contentColor = contentColorFor(containerColor),
        tonalElevation = tonalElevation,
        shape = RectangleShape,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(windowInsets)
                .height(56.dp)
                .padding(PaddingValues()),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) { actions() }
    }
}