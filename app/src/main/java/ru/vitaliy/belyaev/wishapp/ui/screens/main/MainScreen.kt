package ru.vitaliy.belyaev.wishapp.ui.screens.main

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.ui.core.bottombar.WishAppBottomBar
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.main.components.EditModeTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.main.components.WishItemBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.MainScreenState

@ExperimentalCoroutinesApi
@Composable
fun MainScreen(
    openWishDetailed: (WishWithTags) -> Unit,
    onAddWishClicked: () -> Unit,
    onSettingIconClicked: () -> Unit,
    onShareClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val fabShape = RoundedCornerShape(50)
    val state: MainScreenState by viewModel.uiState.collectAsState()

    val topBar: @Composable () -> Unit = if (state.selectedIds.isEmpty()) {
        {
            WishAppTopBar(
                title = stringResource(R.string.app_name),
                actions = {
                    if (BuildConfig.DEBUG) {
                        IconButton(onClick = { viewModel.onAddTestWishClicked() }) {
                            Icon(
                                Filled.Add,
                                contentDescription = "Add test wish"
                            )
                        }
                    }
                    IconButton(onClick = { onSettingIconClicked() }) {
                        Icon(
                            Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    } else {
        {
            EditModeTopBar(
                selectedCount = state.selectedIds.size,
                onCloseEditModeClicked = { viewModel.onCloseEditModeClicked() },
                onDeleteSelectedClicked = { viewModel.onDeleteSelectedClicked() },
                onSelectAllClicked = { viewModel.onSelectAllClicked() },
            )
        }
    }

    Scaffold(
        topBar = topBar,
        bottomBar = { WishAppBottomBar(fabShape, onShareClick) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddWishClicked() },
                shape = fabShape
            ) {
                Icon(Filled.Add, "Add", tint = MaterialTheme.colors.onSurface)
            }
        },
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        val onWishClicked: (WishWithTags) -> Unit = { wish ->
            if (state.selectedIds.isEmpty()) {
                openWishDetailed(wish)
            } else {
                viewModel.onWishLongPress(wish)
            }
        }

        LazyColumn(
            modifier = Modifier.padding(it)
        ) {
            items(state.wishes) { wishItem ->
                val isSelected: Boolean = state.selectedIds.contains(wishItem.wish.id)
                WishItemBlock(
                    wishItem = wishItem,
                    isSelected = isSelected,
                    onWishClicked = onWishClicked,
                    onWishLongPress = { wish -> viewModel.onWishLongPress(wish) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

        }
    }
}

@ExperimentalCoroutinesApi
@Preview
@Composable
fun MainScreenPreview() {
    MainScreen(
        {},
        {},
        {},
        {}
    )
}