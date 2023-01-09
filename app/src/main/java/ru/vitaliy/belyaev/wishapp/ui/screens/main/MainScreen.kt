package ru.vitaliy.belyaev.wishapp.ui.screens.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.database.Tag
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.core.bottombar.WishAppBottomBar
import ru.vitaliy.belyaev.wishapp.ui.core.bottomsheet.WishAppBottomSheet
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.main.components.EmptyWishesPlaceholder
import ru.vitaliy.belyaev.wishapp.ui.screens.main.components.Loader
import ru.vitaliy.belyaev.wishapp.ui.screens.main.components.MainScreenTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.main.components.TagsSheetContent
import ru.vitaliy.belyaev.wishapp.ui.screens.main.components.WishItemBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.MainScreenState
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.MoveDirection
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishesFilter
import ru.vitaliy.belyaev.wishapp.ui.theme.material3.isLight
import ru.vitaliy.belyaev.wishapp.utils.isScrollInInitialState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Composable
fun MainScreen(
    openWishDetailed: (WishWithTags) -> Unit,
    onAddWishClicked: () -> Unit,
    onSettingIconClicked: () -> Unit,
    onShareClick: (List<WishWithTags>) -> Unit,
    onEditTagClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
    appViewModel: AppActivityViewModel = hiltViewModel(LocalContext.current as AppActivity),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden) { state ->
        return@rememberModalBottomSheetState state != ModalBottomSheetValue.HalfExpanded
    }
    val state: MainScreenState by viewModel.uiState.collectAsState()
    val tags: List<Tag> by viewModel.tags.collectAsState()

    val lazyListState = rememberLazyListState()
    val openDeleteConfirmDialog: MutableState<Boolean> = remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        appViewModel.showSnackOnMainFlow.collect {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.showSnackFlow.collect {
            snackbarHostState.showSnackbar(context.getString(it))
        }
    }

    LaunchedEffect(key1 = state) {
        viewModel.scrollInfoFlow.collect {
            lazyListState.scrollToItem(it.position, -it.offset)
        }
    }

    WishAppBottomSheet(
        sheetState = modalBottomSheetState,
        sheetContent = {
            TagsSheetContent(
                modalBottomSheetState = modalBottomSheetState,
                tags = tags,
                wishesFilter = state.wishesFilter,
                onNavItemSelected = { viewModel.onNavItemSelected(it) },
                onEditTagsClicked = onEditTagClick
            )
        },
        modifier = Modifier.navigationBarsPadding()
    ) {

        val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
            topBar = {
                MainScreenTopBar(
                    selectedIds = state.selectedIds,
                    wishesFilter = state.wishesFilter,
                    onSettingIconClicked = onSettingIconClicked,
                    onDeleteSelectedClicked = { openDeleteConfirmDialog.value = true },
                    isScrollInInitialState = { lazyListState.isScrollInInitialState() },
                    topAppBarScrollBehavior = topAppBarScrollBehavior,
                    viewModel = viewModel
                )
            },
            bottomBar = {
                WishAppBottomBar(
                    wishes = state.wishes,
                    wishesFilter = state.wishesFilter,
                    onShareClick = { onShareClick(state.wishes) },
                    onMenuClick = { scope.launch { modalBottomSheetState.animateTo(ModalBottomSheetValue.Expanded) } },
                    reorderButtonState = state.reorderButtonState,
                    onReorderClick = { viewModel.onReorderIconClicked() }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    expanded = false,
                    text = { Text(stringResource(R.string.share_app)) },
                    icon = {
                        ThemedIcon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = "Add",
                        )
                    },
                    onClick = { onAddWishClicked() },
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            val onWishClicked: (WishWithTags) -> Unit = { wish ->
                if (state.selectedIds.isEmpty()) {
                    openWishDetailed(wish)
                } else {
                    viewModel.onWishLongPress(wish)
                }
            }

            val onMoveItem: (WishWithTags, MoveDirection) -> Unit = { wishWithTags, moveDirection ->
                val movedLazyListItemInfo =
                    lazyListState.layoutInfo.visibleItemsInfo.find { info -> info.key == wishWithTags.id }

                viewModel.onMoveWish(
                    movedWish = wishWithTags,
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
                        horizontalPadding = cardsPadding,
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

            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colorScheme.isLight()
            val mainScreenNavBarColor = MaterialTheme.colorScheme.background
            val bottomSheetNavbarColor = MaterialTheme.colorScheme.surface
            LaunchedEffect(key1 = modalBottomSheetState.targetValue) {
                val navbarColor = if (modalBottomSheetState.targetValue != ModalBottomSheetValue.Hidden) {
                    bottomSheetNavbarColor
                } else {
                    mainScreenNavBarColor
                }
//                systemUiController.setNavigationBarColor(
//                    color = navbarColor,
//                    darkIcons = useDarkIcons
//                )
            }

            if (openDeleteConfirmDialog.value) {
                AlertDialog(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.base_corner_radius)),
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
    MainScreen(
        {},
        {},
        {},
        {},
        {},
    )
}