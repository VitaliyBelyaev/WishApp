package ru.vitaliy.belyaev.wishapp.ui.screens.main.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.main.MainViewModel

@ExperimentalCoroutinesApi
@Composable
fun MainScreenTopBar(
    selectedIds: List<String>,
    selectedTag: Tag?,
    onSettingIconClicked: () -> Unit,
    onDeleteSelectedClicked: () -> Unit,
    lazyListState: LazyListState,
    viewModel: MainViewModel
) {
    if (selectedIds.isEmpty()) {
        val title = selectedTag?.title ?: stringResource(R.string.app_name)
        WishAppTopBar(
            title = title,
            lazyListState = lazyListState,
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
            lazyListState = lazyListState
        )
    }
}