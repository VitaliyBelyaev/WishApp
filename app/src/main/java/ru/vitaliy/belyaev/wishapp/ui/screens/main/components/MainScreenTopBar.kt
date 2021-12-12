package ru.vitaliy.belyaev.wishapp.ui.screens.main.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.main.MainViewModel

@ExperimentalCoroutinesApi
@Composable
fun MainScreenTopBar(
    selectedIds: List<String>,
    onSettingIconClicked: () -> Unit,
    viewModel: MainViewModel
) {
    if (selectedIds.isEmpty()) {
        WishAppTopBar(
            title = stringResource(R.string.app_name),
            actions = {
                if (BuildConfig.DEBUG) {
                    IconButton(onClick = { viewModel.onAddTestWishClicked() }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Add test wish"
                        )
                    }
                }
                IconButton(onClick = { onSettingIconClicked() }) {
                    Icon(
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
            onDeleteSelectedClicked = { viewModel.onDeleteSelectedClicked() },
            onSelectAllClicked = { viewModel.onSelectAllClicked() },
        )
    }
}