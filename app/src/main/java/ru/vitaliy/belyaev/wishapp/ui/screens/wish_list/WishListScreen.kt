package ru.vitaliy.belyaev.wishapp.ui.screens.wish_list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagWithWishCount
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.core.alert_dialog.DestructiveConfirmationAlertDialog
import ru.vitaliy.belyaev.wishapp.ui.core.bottomsheet.WishAppBottomSheetM3
import ru.vitaliy.belyaev.wishapp.ui.core.bottomsheet.WishappBottomSheetDefaults
import ru.vitaliy.belyaev.wishapp.ui.core.loader.FullscreenLoaderWithText
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components.EmptyWishesPlaceholder
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components.ShareBottomSheetContent
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components.TagsSheetContent
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components.WishItemBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components.WishListBottomBar
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components.WishListScreenTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.MainScreenState
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.MoveDirection
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.WishesFilter
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
    onAddWishClicked: (tagId: String?) -> Unit,
    onSettingIconClicked: () -> Unit,
    onEditTagClick: () -> Unit,
    onGoToBackupScreenClicked: () -> Unit,
    viewModel: WishListViewModel = hiltViewModel(),
    appViewModel: AppActivityViewModel = hiltViewModel(LocalContext.current as AppActivity),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val state: MainScreenState by viewModel.uiState.collectAsStateWithLifecycle()
    val tagsWithWishCount: List<TagWithWishCount> by viewModel.tagsWithWishCount.collectAsStateWithLifecycle()
    val currentWishesCount: Long by viewModel.currentWishesCount.collectAsStateWithLifecycle()
    val competedWishesCount: Long by viewModel.completedWishesCount.collectAsStateWithLifecycle()

    val lazyListState = rememberLazyListState()
    val openDeleteConfirmDialog: MutableState<Boolean> = remember { mutableStateOf(false) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val systemUiController = rememberSystemUiController()

    val modalNavBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showNavBottomSheet by remember { mutableStateOf(false) }
    val closeNavBottomSheet: () -> Unit = {
        scope.launch { modalNavBottomSheetState.hide() }.invokeOnCompletion {
            if (!modalNavBottomSheetState.isVisible) {
                showNavBottomSheet = false
            }
        }
    }

    val shareChooseBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showShareChooseBottomSheet by remember { mutableStateOf(false) }
    val closeShareChooseBottomSheet: () -> Unit = {
        scope.launch { shareChooseBottomSheetState.hide() }.invokeOnCompletion {
            if (!shareChooseBottomSheetState.isVisible) {
                showShareChooseBottomSheet = false
            }
        }
    }

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

    LaunchedEffect(key1 = Unit) {
        viewModel.hideShareBottomSheetFlow.collect {
            closeShareChooseBottomSheet()
        }
    }

    val mainScreenNavBarColor =
        MaterialTheme.colorScheme.surfaceColorAtElevation(BottomAppBarDefaults.ContainerElevation)

    systemUiController.setNavigationBarColor(color = mainScreenNavBarColor)

    BackHandler(state.wishesFilter != WishesFilter.All) {
        viewModel.onNavItemSelected(WishesFilter.All)
    }

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
            WishListBottomBar(
                wishes = state.wishes,
                wishesFilter = state.wishesFilter,
                onShareClick = {
                    showShareChooseBottomSheet = true
                },
                onMenuClick = { showNavBottomSheet = true },
                reorderButtonState = state.reorderButtonState,
                onReorderClick = { viewModel.onReorderIconClicked() },
                onAddWishClicked = {
                    val tagId = (state.wishesFilter as? WishesFilter.ByTag)?.tag?.id
                    onAddWishClicked.invoke(tagId)
                }
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

        val onMoveItem: (WishEntity, MoveDirection) -> Unit = { wishEntity, moveDirection ->
            val movedLazyListItemInfo =
                lazyListState.layoutInfo.visibleItemsInfo.find { info -> info.key == wishEntity.id }

            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

            viewModel.onMoveWish(
                movedWish = wishEntity,
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
                    horizontalOuterPadding = 8.dp,
                    onWishClicked = onWishClicked,
                    onWishLongPress = { wish ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onWishLongPress(wish)
                    },
                    state.reorderButtonState,
                    onMoveItem = onMoveItem,
                    modifier = Modifier
                        .padding(top = cardsPadding, bottom = bottomPadding)
                )
            }
        }

        if (state.wishes.isEmpty() && !state.isLoading) {
            val filter = state.wishesFilter
            val emptyMessage: String = when (filter) {
                is WishesFilter.All -> stringResource(id = R.string.empty_current_wishes_message)
                is WishesFilter.ByTag -> stringResource(id = R.string.empty_tagged_wishes_message, filter.tag.title)
                is WishesFilter.Completed -> stringResource(id = R.string.empty_done_wishes_message)
            }
            EmptyWishesPlaceholder(
                text = emptyMessage,
                modifier = Modifier.padding(paddingValues),
                showBackupSection = filter is WishesFilter.All,
                onGoToBackupScreenClicked = {
                    viewModel.onGoToBackupScreenClicked()
                    onGoToBackupScreenClicked()
                }
            )
        }

        if (state.isLoading) {
            FullscreenLoaderWithText(modifier = Modifier.padding(paddingValues))
        }

        if (openDeleteConfirmDialog.value) {
            DestructiveConfirmationAlertDialog(
                onDismissRequest = { openDeleteConfirmDialog.value = false },
                title = { Text(stringResource(R.string.delete_wishes_title)) },
                confirmClick = {
                    openDeleteConfirmDialog.value = false
                    viewModel.onDeleteSelectedClicked()
                },
            )
        }

        val navigationBottomPadding = WishappBottomSheetDefaults.navigationBottomPadding()
        if (showNavBottomSheet) {
            WishAppBottomSheetM3(
                onDismissRequest = { showNavBottomSheet = false },
                sheetState = modalNavBottomSheetState,
            ) {
                TagsSheetContent(
                    tagsWithWishCount = tagsWithWishCount,
                    wishesFilter = state.wishesFilter,
                    currentWishesCount = currentWishesCount,
                    completedWishesCount = competedWishesCount,
                    onNavItemSelected = {
                        closeNavBottomSheet()
                        viewModel.onNavItemSelected(it)
                    },
                    onEditTagsClicked = {
                        closeNavBottomSheet()
                        onEditTagClick()
                    },
                    modifier = Modifier.padding(bottom = navigationBottomPadding)
                )
            }
        }

        if (showShareChooseBottomSheet) {
            WishAppBottomSheetM3(
                onDismissRequest = { showShareChooseBottomSheet = false },
                sheetState = shareChooseBottomSheetState,
            ) {
                ShareBottomSheetContent(
                    wishesToShare = state.wishes,
                    onShareClick = { shareData ->
                        viewModel.onShareClick(context, shareData)
                    },
                    isPdfShareButtonLoading = state.isShareAsPdfLoading,
                    modifier = Modifier.padding(bottom = navigationBottomPadding)
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