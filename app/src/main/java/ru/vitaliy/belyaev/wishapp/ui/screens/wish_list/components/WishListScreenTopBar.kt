package ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.WishListViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.WishesFilter

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalCoroutinesApi
@Composable
fun WishListScreenTopBar(
    selectedIds: List<String>,
    wishesFilter: WishesFilter,
    onSettingIconClicked: () -> Unit,
    onDeleteSelectedClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    viewModel: WishListViewModel
) {
    if (selectedIds.isEmpty()) {
        val title = when (wishesFilter) {
            is WishesFilter.ByTag -> wishesFilter.tag.title
            is WishesFilter.All -> stringResource(R.string.app_name)
            is WishesFilter.Completed -> stringResource(R.string.completed_wishes)
        }
        WishAppTopBar(
            title = title,
            scrollBehavior = scrollBehavior,
            actions = {
                if (BuildConfig.DEBUG) {
                    IconButton(onClick = { viewModel.onAddTestWishClicked() }) {
                        ThemedIcon(
                            painterResource(R.drawable.ic_add),
                            contentDescription = "Add test wish"
                        )
                    }
                }
                IconButton(onClick = { onSettingIconClicked() }) {
                    ThemedIcon(
                        painterResource(R.drawable.ic_settings),
                        contentDescription = "Settings"
                    )
                }
            }
        )
    } else {
        EditModeTopBar(
            selectedCount = selectedIds.size,
            onCloseEditModeClicked = { viewModel.onCloseEditModeClicked() },
            onDeleteSelectedClicked = onDeleteSelectedClicked,
            onSelectAllClicked = { viewModel.onSelectAllClicked() },
            scrollBehavior = scrollBehavior
        )
    }
}