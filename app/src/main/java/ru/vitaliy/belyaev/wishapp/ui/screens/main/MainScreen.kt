package ru.vitaliy.belyaev.wishapp.ui.screens.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberModalBottomSheetState
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
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.database.Tag
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.core.bottombar.WishAppBottomBar
import ru.vitaliy.belyaev.wishapp.ui.core.bottomsheet.WishAppBottomSheet
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.main.components.MainScreenTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.main.components.TagsSheetContent
import ru.vitaliy.belyaev.wishapp.ui.screens.main.components.WishItemBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.MainScreenState
import ru.vitaliy.belyaev.wishapp.ui.theme.localTheme
import ru.vitaliy.belyaev.wishapp.utils.isScrollInInitialState

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
    val fabShape = RoundedCornerShape(50)
    val scope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden) { state ->
        return@rememberModalBottomSheetState state != ModalBottomSheetValue.HalfExpanded
    }
    val state: MainScreenState by viewModel.uiState.collectAsState()
    val tags: List<Tag> by viewModel.tags.collectAsState()
    val reorderableListState = rememberReorderableLazyListState(
        onMove = { from, to -> viewModel.onMove(from, to) }
    )
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
        Scaffold(
            topBar = {
                MaterialTheme.colors.primary
                MainScreenTopBar(
                    selectedIds = state.selectedIds,
                    wishesFilter = state.wishesFilter,
                    onSettingIconClicked = onSettingIconClicked,
                    onDeleteSelectedClicked = { openDeleteConfirmDialog.value = true },
                    isScrollInInitialState = { reorderableListState.listState.isScrollInInitialState() },
                    viewModel = viewModel
                )
            },
            bottomBar = {
                WishAppBottomBar(
                    wishesFilter = state.wishesFilter,
                    cutoutShape = fabShape,
                    onShareClick = { onShareClick(state.wishes) },
                    onMenuClick = { scope.launch { modalBottomSheetState.animateTo(ModalBottomSheetValue.Expanded) } },
                    isReorderEnabled = state.isReorderEnabled,
                    onReorderClick = { viewModel.onReorderIconClicked() }
                )
            },
            floatingActionButton = {
                val elevation = if (MaterialTheme.colors.isLight) {
                    FloatingActionButtonDefaults.elevation()
                } else {
                    FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
                }
                FloatingActionButton(
                    onClick = { onAddWishClicked() },
                    backgroundColor = MaterialTheme.colors.surface,
                    shape = fabShape,
                    elevation = elevation
                ) {
                    ThemedIcon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = "Add",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            isFloatingActionButtonDocked = true,
            floatingActionButtonPosition = FabPosition.Center,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) {
            val onWishClicked: (WishWithTags) -> Unit = { wish ->
                if (state.selectedIds.isEmpty()) {
                    openWishDetailed(wish)
                } else {
                    viewModel.onWishLongPress(wish)
                }
            }

            val cardsPadding = 10.dp

            LazyColumn(
                state = reorderableListState.listState,
                modifier = Modifier
                    .padding(it)
                    .reorderable(reorderableListState)

            ) {
                item {
                    Spacer(modifier = Modifier.height(cardsPadding))
                }
                items(state.wishes, { wish -> wish.id }) { wishItem ->
                    ReorderableItem(
                        reorderableState = reorderableListState,
                        key = wishItem.id
                    ) {
                        val isSelected: Boolean = state.selectedIds.contains(wishItem.id)
                        WishItemBlock(
                            wishItem = wishItem,
                            isSelected = isSelected,
                            paddingValues = PaddingValues(horizontal = cardsPadding),
                            onWishClicked = onWishClicked,
                            onWishLongPress = { wish -> viewModel.onWishLongPress(wish) },
                            reorderableListState,
                            state.isReorderEnabled,
                        )
                    }
                    Spacer(modifier = Modifier.height(cardsPadding))
                }
                item {
                    Spacer(modifier = Modifier.height(84.dp))
                }
            }

            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colors.isLight
            val mainScreenNavBarColor = localTheme.colors.navigationBarColor
            val bottomSheetNavbarColor = localTheme.colors.bottomSheetBackgroundColor
            LaunchedEffect(key1 = modalBottomSheetState.targetValue) {
                val navbarColor = if (modalBottomSheetState.targetValue != ModalBottomSheetValue.Hidden) {
                    bottomSheetNavbarColor
                } else {
                    mainScreenNavBarColor
                }
                systemUiController.setNavigationBarColor(
                    color = navbarColor,
                    darkIcons = useDarkIcons
                )
            }

            if (openDeleteConfirmDialog.value) {
                AlertDialog(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.base_corner_radius)),
                    backgroundColor = localTheme.colors.bottomSheetBackgroundColor,
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
                                color = MaterialTheme.colors.onSurface
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { openDeleteConfirmDialog.value = false }
                        ) {
                            Text(
                                stringResource(R.string.cancel),
                                color = MaterialTheme.colors.onSurface
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