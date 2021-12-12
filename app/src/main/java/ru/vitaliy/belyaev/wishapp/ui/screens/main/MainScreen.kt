package ru.vitaliy.belyaev.wishapp.ui.screens.main

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.ui.core.bottombar.WishAppBottomBar
import ru.vitaliy.belyaev.wishapp.ui.screens.main.components.MainScreenTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.main.components.TagsSheetContent
import ru.vitaliy.belyaev.wishapp.ui.screens.main.components.WishItemBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.MainScreenState
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.NavigationMenuItem

@ExperimentalMaterialApi
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
    val scope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val state: MainScreenState by viewModel.uiState.collectAsState()
    val navMenuItems: List<NavigationMenuItem> by viewModel.navigationMenuUiState.collectAsState()

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        sheetBackgroundColor = colorResource(R.color.bottomSheetBgColor),
        scrimColor = Color.Black.copy(alpha = 0.32f),
        sheetContent = {
            TagsSheetContent(
                modalBottomSheetState = modalBottomSheetState,
                navMenuItems = navMenuItems,
                onNavItemSelected = { viewModel.onNavItemSelected(it) },
                onEditTagsClicked = { viewModel.onEditTagsClicked() }
            )
        }
    ) {
        Scaffold(
            topBar = {
                MainScreenTopBar(
                    selectedIds = state.selectedIds,
                    selectedTag = state.selectedTag,
                    onSettingIconClicked = onSettingIconClicked,
                    viewModel = viewModel
                )
            },
            bottomBar = {
                WishAppBottomBar(
                    cutoutShape = fabShape,
                    onShareClick = onShareClick,
                    onMenuClick = { scope.launch { modalBottomSheetState.show() } }
                )
            },
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
                    val isSelected: Boolean = state.selectedIds.contains(wishItem.id)
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
}

@ExperimentalMaterialApi
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