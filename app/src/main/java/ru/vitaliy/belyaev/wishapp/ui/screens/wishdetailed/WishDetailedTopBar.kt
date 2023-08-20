package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.WishItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishDetailedTopBar(
    onBackPressed: () -> Unit,
    wishItem: WishItem?,
    onWishTagsClicked: (String) -> Unit,
    onDeleteClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    WishAppTopBar(
        "",
        withBackIcon = true,
        onBackPressed = onBackPressed,
        actions = {
            IconButton(
                onClick = {
                    val wishId = wishItem?.wish?.id ?: return@IconButton
                    onWishTagsClicked(wishId)
                }
            ) {
                ThemedIcon(
                    painterResource(R.drawable.ic_label),
                    contentDescription = "Open tags"
                )
            }
            IconButton(onClick = onDeleteClicked) {
                ThemedIcon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete wish"
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}