package ru.vitaliy.belyaev.wishapp.ui.screens.main.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.main.MainViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishesFilter

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalCoroutinesApi
@Composable
fun MainScreenTopBar(
    selectedIds: List<String>,
    wishesFilter: WishesFilter,
    onSettingIconClicked: () -> Unit,
    onDeleteSelectedClicked: () -> Unit,
    isScrollInInitialState: (() -> Boolean),
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    viewModel: MainViewModel
) {
    if (selectedIds.isEmpty()) {
        val title = when (wishesFilter) {
            is WishesFilter.ByTag -> wishesFilter.tag.title
            is WishesFilter.All -> stringResource(R.string.app_name)
            is WishesFilter.Completed -> stringResource(R.string.completed_wishes)
        }
        WishAppTopBar(
            title = title,
            isScrollInInitialState = isScrollInInitialState,
            topAppBarScrollBehavior = topAppBarScrollBehavior,
            actions = {
                if (BuildConfig.DEBUG) {
                    IconButton(onClick = { viewModel.onAddTestWishClicked() }) {
                        ThemedIcon(
                            Icons.Filled.Add,
                            contentDescription = "Add test wish"
                        )
                    }
                }
                IconButton(onClick = { onSettingIconClicked() }) {
                    ThemedIcon(
                        Icons.Filled.Settings,
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
            isScrollInInitialState = isScrollInInitialState,
            topAppBarScrollBehavior = topAppBarScrollBehavior
        )
    }
}