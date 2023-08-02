package ru.vitaliy.belyaev.wishapp.ui.screens.wish_list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagWithWishCount
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.core.bottombar.WishAppBottomBar
import ru.vitaliy.belyaev.wishapp.ui.core.bottomsheet.WishAppBottomSheet
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components.EmptyWishesPlaceholder
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components.Loader
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components.TagsSheetContent
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components.WishItemBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components.WishListScreenTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.MainScreenState
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.MoveDirection
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.WishesFilter
import ru.vitaliy.belyaev.wishapp.ui.theme.CommonColors
import ru.vitaliy.belyaev.wishapp.utils.showDismissableSnackbar
import ru.vitaliy.belyaev.wishapp.utils.trackScreenShow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Composable
fun WishListScreen(
    openWishDetailed: (WishEntity) -> Unit,
    onAddWishClicked: () -> Unit,
    onSettingIconClicked: () -> Unit,
    onShareClick: (List<WishEntity>) -> Unit,
    onEditTagClick: () -> Unit,
    viewModel: WishListViewModel = hiltViewModel(),
    appViewModel: AppActivityViewModel = hiltViewModel(LocalContext.current as AppActivity),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val modalBottomSheetState =
        rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true
        )
    val state: MainScreenState by viewModel.uiState.collectAsState()
    val tagsWithWishCount: List<TagWithWishCount> by viewModel.tagsWithWishCount.collectAsState()
    val currentWishesCount: Long by viewModel.currentWishesCount.collectAsState()
    val competedWishesCount: Long by viewModel.completedWishesCount.collectAsState()

    val lazyListState = rememberLazyListState()
    val openDeleteConfirmDialog: MutableState<Boolean> = remember { mutableStateOf(false) }

    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()

    trackScreenShow { viewModel.trackScreenShow() }

    LaunchedEffect(key1 = Unit) {
        appViewModel.showSnackOnMainFlow.collect {
            snackbarHostState.showDismissableSnackbar(it)
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.showSnackFlow.collect {
            snackbarHostState.showDismissableSnackbar(context.getString(it))
        }
    }

    LaunchedEffect(key1 = state) {
        viewModel.scrollInfoFlow.collect {
            lazyListState.scrollToItem(it.position, -it.offset)
        }
    }

    val mainScreenNavBarColor =
        MaterialTheme.colorScheme.surfaceColorAtElevation(BottomAppBarDefaults.ContainerElevation)
    val bottomSheetNavbarColor = CommonColors.bottomSheetBgColor()

    LaunchedEffect(key1 = Unit) {
        val navbarColor = if (modalBottomSheetState.targetValue != ModalBottomSheetValue.Hidden) {
            bottomSheetNavbarColor
        } else {
            mainScreenNavBarColor
        }
        systemUiController.setNavigationBarColor(color = navbarColor)
    }

    WishAppBottomSheet(
        sheetState = modalBottomSheetState,
        sheetContent = {
            TagsSheetContent(
                modalBottomSheetState = modalBottomSheetState,
                tagsWithWishCount = tagsWithWishCount,
                wishesFilter = state.wishesFilter,
                currentWishesCount = currentWishesCount,
                completedWishesCount = competedWishesCount,
                onNavItemSelected = { viewModel.onNavItemSelected(it) },
                onEditTagsClicked = onEditTagClick,
                modifier = Modifier.safeDrawingPadding()
            )
        },
    ) {
        val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
            contentWindowInsets = WindowInsets.Companion.safeDrawing,
            topBar = {
                WishListScreenTopBar(
                    selectedIds = state.selectedIds,
                    wishesFilter = state.wishesFilter,
                    onSettingIconClicked = onSettingIconClicked,
                    onDeleteSelectedClicked = { openDeleteConfirmDialog.value = true },
                    scrollBehavior = topAppBarScrollBehavior,
                    viewModel = viewModel
                )
            },
            bottomBar = {
                WishAppBottomBar(
                    wishes = state.wishes,
                    wishesFilter = state.wishesFilter,
                    onShareClick = { onShareClick(state.wishes) },
                    onMenuClick = { scope.launch { modalBottomSheetState.show() } },
                    reorderButtonState = state.reorderButtonState,
                    onReorderClick = { viewModel.onReorderIconClicked() },
                    onAddWishClicked = onAddWishClicked
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            val onWishClicked: (WishEntity) -> Unit = { wish ->
                if (state.selectedIds.isEmpty()) {
                    openWishDetailed(wish)
                } else {
                    viewModel.onWishLongPress(wish)
                }
            }

            val onMoveItem: (WishEntity, MoveDirection) -> Unit = { WishEntity, moveDirection ->
                val movedLazyListItemInfo =
                    lazyListState.layoutInfo.visibleItemsInfo.find { info -> info.key == WishEntity.id }

                viewModel.onMoveWish(
                    movedWish = WishEntity,
                    moveDirection = moveDirection,
                    scrollOffset = movedLazyListItemInfo?.offset ?: 0
                )
            }

            val cardsPadding = 10.dp

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .padding(paddingValues)
                    .nestedScroll(connection = object : NestedScrollConnection {})
            ) {

                itemsIndexed(
                    items = state.wishes,
                    key = { _, wishItem -> wishItem.id })
                { index, wishItem ->
                    if (index == 0) {
                        Spacer(modifier = Modifier.height(cardsPadding))
                    }
                    val isSelected: Boolean = state.selectedIds.contains(wishItem.id)
                    val bottomPadding = if (index == state.wishes.lastIndex) {
                        36.dp
                    } else {
                        0.dp
                    }
                    WishItemBlock(
                        wishItem = wishItem,
                        isSelected = isSelected,
                        horizontalPadding = 12.dp,
                        onWishClicked = onWishClicked,
                        onWishLongPress = { wish -> viewModel.onWishLongPress(wish) },
                        state.reorderButtonState,
                        onMoveItem = onMoveItem,
                        modifier = Modifier
                            .padding(top = cardsPadding, bottom = bottomPadding)
                    )
                }
            }

            if (state.wishes.isEmpty() && !state.isLoading) {
                val emptyMessage: String = when (val filter = state.wishesFilter) {
                    is WishesFilter.All -> stringResource(id = R.string.empty_current_wishes_message)
                    is WishesFilter.ByTag -> stringResource(id = R.string.empty_tagged_wishes_message, filter.tag.title)
                    is WishesFilter.Completed -> stringResource(id = R.string.empty_done_wishes_message)
                }
                EmptyWishesPlaceholder(
                    text = emptyMessage,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            if (state.isLoading) {
                Loader(modifier = Modifier.padding(paddingValues))
            }

            if (openDeleteConfirmDialog.value) {
                AlertDialog(
                    onDismissRequest = { openDeleteConfirmDialog.value = false },
                    title = { Text(stringResource(R.string.delete_wishes_title)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                openDeleteConfirmDialog.value = false
                                viewModel.onDeleteSelectedClicked()
                            }
                        ) {
                            Text(
                                stringResource(R.string.delete),
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { openDeleteConfirmDialog.value = false }
                        ) {
                            Text(
                                stringResource(R.string.cancel),
                            )
                        }
                    }
                )
            }
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Preview
@Composable
fun MainScreenPreview() {
    WishListScreen(
        {},
        {},
        {},
        {},
        {},
    )
}