package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.ListLayout
import ru.vitaliy.belyaev.wishapp.ui.components.dock.DockTab
import ru.vitaliy.belyaev.wishapp.ui.components.dock.FloatingDock
import ru.vitaliy.belyaev.wishapp.ui.components.dock.FloatingDockFade
import ru.vitaliy.belyaev.wishapp.ui.components.dock.FloatingDockHeight
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishCardModel
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishItemDrag
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishSelection
import ru.vitaliy.belyaev.wishapp.ui.components.wish.card.WishCard
import ru.vitaliy.belyaev.wishapp.ui.components.wish.row.WishRow
import ru.vitaliy.belyaev.wishapp.ui.components.wish.toPaperTone
import ru.vitaliy.belyaev.wishapp.ui.core.snackbar.AppSnackMessage
import ru.vitaliy.belyaev.wishapp.ui.screens.newwish.NewWishOrigin
import ru.vitaliy.belyaev.wishapp.ui.screens.newwish.NewWishSheetHost
import ru.vitaliy.belyaev.wishapp.ui.screens.newwish.NewWishViewModel
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyStaggeredGridState

/** Куда список ведёт. Пока новых экранов нет, это экраны прежнего вида. */
class WishListActions(
    val onWishClick: (wishId: String) -> Unit,
    val onSettings: () -> Unit,
    val onManageTags: () -> Unit,
    val onRestore: () -> Unit,
    val onShowMessage: (AppSnackMessage) -> Unit,
)

/** Режим выбора и перенос карточек. */
private interface SelectionCallbacks {
    fun onToggle(wishId: String)
    fun onLongPress(wishId: String)
    fun onSelectAll()
    fun onClear()
    fun onBulkAction(action: BulkAction)
    fun onMoved(wishId: String, toIndex: Int)
    fun onMovedBy(wishId: String, step: Int)
}

private enum class OpenSheet { NONE, TAGS, SORT, SHARE }

@Composable
fun WishListScreen(
    actions: WishListActions,
    viewModel: WishListViewModel = hiltViewModel(),
    newWishViewModel: NewWishViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isSharingPdf by viewModel.isSharingPdf.collectAsStateWithLifecycle()
    val dockSection by viewModel.dockSection.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    val currentActions by rememberUpdatedState(actions)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                WishListEvent.SharePdfFailed -> AppSnackMessage(resources.getString(R.string.share_pdf_error_message))
                is WishListEvent.Undoable -> AppSnackMessage(
                    text = resources.getString(
                        when (event.action) {
                            BulkAction.DELETE -> R.string.snack_deleted_count
                            BulkAction.CAME_TRUE -> R.string.snack_came_true_count
                            BulkAction.WISH_AGAIN -> R.string.snack_wish_again_count
                        },
                        event.count,
                    ),
                    actionLabel = resources.getString(R.string.snack_undo),
                    onAction = event.onUndo,
                    onDismiss = event.onConfirm,
                    // Удаление без «Вернуть» не исправить, «Сбылось» легко отменить с экрана хотелки
                    duration = if (event.action == BulkAction.DELETE) SnackbarDuration.Long else SnackbarDuration.Short,
                )
            }
            currentActions.onShowMessage(message)
        }
    }

    WishListContent(
        state = state,
        dockSection = dockSection,
        isSharingPdf = isSharingPdf,
        actions = actions,
        onAddWish = newWishViewModel::open,
        onSectionSelected = viewModel::onSectionSelected,
        onSearchOpened = viewModel::onSearchOpened,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSearchClosed = viewModel::onSearchClosed,
        onGoToOtherSection = viewModel::onGoToOtherSectionClicked,
        onLayoutToggled = viewModel::onLayoutToggled,
        onSortSelected = viewModel::onSortSelected,
        onShare = { format -> viewModel.onShareClicked(context, format) },
        selection = remember(viewModel) {
            object : SelectionCallbacks {
                override fun onToggle(wishId: String) = viewModel.onWishToggled(wishId)
                override fun onLongPress(wishId: String) = viewModel.onWishLongPressed(wishId)
                override fun onSelectAll() = viewModel.onSelectAllClicked()
                override fun onClear() = viewModel.onSelectionCleared()
                override fun onBulkAction(action: BulkAction) = viewModel.onBulkActionClicked(action)
                override fun onMoved(wishId: String, toIndex: Int) = viewModel.onWishMoved(wishId, toIndex)
                override fun onMovedBy(wishId: String, step: Int) = viewModel.onWishMovedBy(wishId, step)
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun WishListContent(
    state: WishListUiState,
    dockSection: ListSection,
    isSharingPdf: Boolean,
    actions: WishListActions,
    onAddWish: (contextTagId: String?, title: String?) -> Unit,
    onSectionSelected: (ListSection) -> Unit,
    onSearchOpened: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSearchClosed: () -> Unit,
    onGoToOtherSection: () -> Unit,
    onLayoutToggled: () -> Unit,
    onSortSelected: (SectionSort) -> Unit,
    onShare: (ShareFormat) -> Unit,
    selection: SelectionCallbacks,
) {
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    var openSheet by rememberSaveable { mutableStateOf(OpenSheet.NONE) }
    // После «Управления» шторка ярлыков снова открыта, когда возвращаемся на список. Уходя, шторку
    // закрываем: окно шторки иначе висело бы поверх экрана управления. Экран списка при уходе
    // покидает композицию, поэтому [returnedFromManage] берёт флаг, сохранённый до ухода
    var reopenTagsOnReturn by rememberSaveable { mutableStateOf(false) }
    val returnedFromManage = remember { reopenTagsOnReturn }
    LaunchedEffect(Unit) {
        if (returnedFromManage) {
            reopenTagsOnReturn = false
            openSheet = OpenSheet.TAGS
        }
    }
    val search = state.search
    // Панель новой хотелки растёт из плюса, когда открыта им, иначе из нижнего края
    val newWishOrigin = remember { NewWishOrigin() }
    var addButtonBounds by remember { mutableStateOf<Rect?>(null) }

    // У раздела и сортировки своя сетка, раздел открывается там, где его оставили.
    // Поиск в ключ не входит: новая сетка на каждую букву заново строила карточки и грузила
    // картинки, и список мигал. Фильтр оставляет ту же сетку, а с начала её открывает смена запроса
    val listKey = "${state.section}|${state.sort}"
    val gridState = rememberSectionGridState(
        section = state.section,
        sort = state.sort,
        isFiltered = !search?.query.isNullOrEmpty(),
    )
    val topLayout = rememberTopLayout(gridState)

    // Пустой запрос показывает тот же список, что и без поиска, поэтому открытие поиска
    // прокрутку не сбрасывает
    val searchScrollKey = search?.query?.ifEmpty { null }
    var lastSearchScrollKey by rememberSaveable(listKey) { mutableStateOf(searchScrollKey) }
    LaunchedEffect(searchScrollKey) {
        if (searchScrollKey != lastSearchScrollKey) {
            lastSearchScrollKey = searchScrollKey
            gridState.scrollToItem(0)
        }
    }

    // Прокрутка в поиске прячет клавиатуру, чтобы видеть результаты
    LaunchedEffect(gridState.isScrollInProgress) {
        if (gridState.isScrollInProgress && search != null) keyboard?.hide()
    }
    // Из «Сбылось» и ярлыка системное «назад» ведёт в «Хотелки», а уже оттуда из приложения.
    // Поиск, шторки и меню объявлены ниже и перехватывают «назад» раньше
    BackHandler(enabled = search == null && state.section != ListSection.Wishes) {
        onSectionSelected(ListSection.Wishes)
    }
    BackHandler(enabled = search != null, onBack = onSearchClosed)
    // Объявлен последним и перехватывает «назад» первым: сначала снимается выбор
    BackHandler(enabled = state.isSelectionMode, onBack = selection::onClear)

    val sectionTitle = when (state.section) {
        ListSection.Wishes -> stringResource(R.string.nav_wishes)
        ListSection.Done -> stringResource(R.string.nav_done)
        is ListSection.Tag -> state.selectedTag?.title.orEmpty()
    }
    val shareScope = if (state.isSelectionMode) {
        stringResource(R.string.selection_count, state.selectedIds.size)
    } else {
        stringResource(
            R.string.share_scope,
            sectionTitle,
            pluralStringResource(R.plurals.wishes_count, state.cards.size, state.cards.size),
        )
    }

    val density = LocalDensity.current
    val collapsePx = with(density) { HeaderCollapseDistance.toPx() }
    val progress: () -> Float = {
        if (gridState.firstVisibleItemIndex > 0) {
            1f
        } else {
            (gridState.firstVisibleItemScrollOffset / collapsePx).coerceIn(0f, 1f)
        }
    }

    val navBarBottom = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
    val contentPadding = PaddingValues(
        top = headerHeight() + WishAppSpacing.s2,
        bottom = navBarBottom + FloatingDockHeight + WishAppSpacing.s3 * 2,
        start = WishAppSpacing.s3,
        end = WishAppSpacing.s3,
    )

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> Unit
            state.sectionIsEmpty -> EmptySection(
                section = state.section,
                contentPadding = contentPadding,
                onAddClick = {
                    newWishOrigin.bounds = null
                    onAddWish(state.selectedTag?.id, null)
                },
                onRestoreClick = actions.onRestore,
            )

            search != null && search.query.isNotBlank() && state.cards.isEmpty() -> Box(Modifier.imePadding()) {
                EmptySearchResult(
                    query = search.query,
                    section = state.section,
                    sectionTitle = sectionTitle,
                    otherSectionMatches = search.otherSectionMatches,
                    contentPadding = contentPadding,
                    onGoToOtherSection = onGoToOtherSection,
                    onCreateClick = {
                        val title = search.query.trim()
                        onSearchClosed()
                        newWishOrigin.bounds = null
                        onAddWish(state.selectedTag?.id, title)
                    },
                )
            }

            else -> key(listKey) {
                WishGrid(
                    state = state,
                    gridState = gridState,
                    contentPadding = contentPadding,
                    onWishClick = actions.onWishClick,
                    selection = selection,
                )
            }
        }

        if (state.isSelectionMode) {
            SelectionHeader(
                count = state.selectedIds.size,
                progress = progress,
                onClear = selection::onClear,
                onSelectAll = selection::onSelectAll,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        } else if (search != null) {
            SearchHeader(
                query = search.query,
                hint = when (state.section) {
                    ListSection.Wishes -> stringResource(R.string.search_hint)
                    else -> stringResource(R.string.search_in, sectionTitle)
                },
                onQueryChange = onSearchQueryChanged,
                onClose = onSearchClosed,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        } else {
            val showShareIcon = state.section is ListSection.Tag && !state.sectionIsEmpty
            val hasContent = !state.sectionIsEmpty && !state.isLoading
            val isCards = state.layout == ListLayout.CARDS
            val menu = HeaderMenuState(
                showShare = hasContent && !showShareIcon,
                // В меню коротко, «Хотелки: 34», полная подпись в самом меню «Поделиться»
                shareHint = stringResource(R.string.share_scope, sectionTitle, state.cards.size.toString()),
                sortLabel = sortLabel(state.sort).takeIf { hasContent },
                showSortDot = hasContent && !state.sort.isDefault,
                layoutLabel = stringResource(if (isCards) R.string.menu_show_list else R.string.menu_show_cards)
                    .takeIf { hasContent },
                layoutIcon = if (isCards) R.drawable.ic_ms_view_agenda else R.drawable.ic_ms_grid_view,
                onShare = { openSheet = OpenSheet.SHARE },
                onSort = { openSheet = OpenSheet.SORT },
                onToggleLayout = onLayoutToggled,
                onSettings = actions.onSettings,
            )
            WishListHeader(
                title = sectionTitle,
                isTagTitle = state.section is ListSection.Tag,
                progress = progress,
                showShareIcon = showShareIcon,
                menu = menu,
                onSearchClick = onSearchOpened,
                onShareClick = { openSheet = OpenSheet.SHARE },
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        // Док не прячется под клавиатурой: она закрывает его сама, а уходя, открывает на месте, без
        // выезда. Поиск прячет клавиатуру при прокрутке, так что док под ней не нужен
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            Box(contentAlignment = Alignment.BottomCenter) {
                FloatingDockFade()
                // Полоса под панелью вместе с её отступами не пропускает касания к списку:
                // при сворачивании приложения жестом список не прокручивается и карточки не нажимаются
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(navBarBottom + FloatingDockHeight + WishAppSpacing.s3 * 2)
                        .blockTouches(),
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            val dockModifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = WishAppSpacing.s3)
                .padding(bottom = WishAppSpacing.s3)
            if (state.isSelectionMode) {
                SelectionDock(
                    isDoneSection = state.section == ListSection.Done,
                    onCompleteToggle = {
                        selection.onBulkAction(
                            if (state.section == ListSection.Done) BulkAction.WISH_AGAIN else BulkAction.CAME_TRUE,
                        )
                    },
                    onShare = { openSheet = OpenSheet.SHARE },
                    onDelete = { selection.onBulkAction(BulkAction.DELETE) },
                    modifier = dockModifier,
                )
                return@Box
            }
            FloatingDock(
                selected = when (dockSection) {
                    ListSection.Wishes -> DockTab.Wishes
                    ListSection.Done -> DockTab.Done
                    is ListSection.Tag ->
                        state.tags.firstOrNull { it.tag.id == dockSection.tagId }?.tag
                            ?.let { DockTab.Tag(it.title, it.color.toPaperTone()) }
                            ?: DockTab.Wishes
                },
                doneCount = state.doneCount,
                // Нажатие на уже открытую вкладку поднимает список наверх. У «Ярлыков» так нет:
                // вкладка открывает шторку
                onWishesClick = {
                    if (dockSection == ListSection.Wishes) {
                        scope.launch { gridState.scrollToTopSmoothly(topLayout) }
                    } else {
                        onSectionSelected(ListSection.Wishes)
                    }
                },
                onDoneClick = {
                    if (dockSection == ListSection.Done) {
                        scope.launch { gridState.scrollToTopSmoothly(topLayout) }
                    } else {
                        onSectionSelected(ListSection.Done)
                    }
                },
                onTagsClick = { openSheet = OpenSheet.TAGS },
                onAddClick = {
                    newWishOrigin.bounds = addButtonBounds
                    onAddWish(state.selectedTag?.id, null)
                },
                modifier = dockModifier,
                // Пока панель на месте плюса, сам плюс не виден: панель и есть поднявшийся плюс
                addButtonModifier = Modifier
                    .onGloballyPositioned { addButtonBounds = it.boundsInRoot() }
                    .graphicsLayer { alpha = if (newWishOrigin.isLifted) 0f else 1f },
            )
        }

        if (openSheet == OpenSheet.SHARE) {
            // Меню закрывается, когда PDF собран и отдан системе или не получился
            var wasSharingPdf by remember { mutableStateOf(false) }
            LaunchedEffect(isSharingPdf) {
                if (wasSharingPdf && !isSharingPdf) openSheet = OpenSheet.NONE
                wasSharingPdf = isSharingPdf
            }
        }
        // Меню остаётся в композиции и закрывается своей анимацией
        ShareMenu(
            visible = openSheet == OpenSheet.SHARE,
            // Из панели выбора меню поднимается снизу, из шапки выпадает сверху
            anchor = if (state.isSelectionMode) ShareMenuAnchor.BOTTOM else ShareMenuAnchor.TOP,
            scope = shareScope,
            isSharingPdf = isSharingPdf,
            onShareText = {
                openSheet = OpenSheet.NONE
                onShare(ShareFormat.TEXT)
            },
            onSharePdf = { onShare(ShareFormat.PDF) },
            // Отметить часть есть смысл, когда хотелок хотя бы две. Выбор открывается со всеми
            // отмеченными: обычно лишнее снимают, а не набирают нужное с нуля
            onPickPart = if (!state.isSelectionMode && state.cards.size >= MIN_CARDS_TO_PICK_PART) {
                {
                    openSheet = OpenSheet.NONE
                    selection.onSelectAll()
                }
            } else {
                null
            },
            onDismiss = { if (!isSharingPdf) openSheet = OpenSheet.NONE },
        )
    }

    when (openSheet) {
        OpenSheet.TAGS -> TagsSheet(
            tags = state.tags,
            selectedTagId = state.selectedTag?.id,
            onTagClick = { tagId ->
                openSheet = OpenSheet.NONE
                onSectionSelected(ListSection.Tag(tagId))
            },
            onManageClick = {
                openSheet = OpenSheet.NONE
                reopenTagsOnReturn = true
                actions.onManageTags()
            },
            onDismiss = { openSheet = OpenSheet.NONE },
        )

        OpenSheet.SORT -> SortSheet(
            current = state.sort,
            onSelect = { sort ->
                openSheet = OpenSheet.NONE
                onSortSelected(sort)
            },
            onDismiss = { openSheet = OpenSheet.NONE },
        )

        OpenSheet.NONE, OpenSheet.SHARE -> Unit
    }

    // Новая карточка встаёт в начало списка: показываем её
    NewWishSheetHost(
        onShowMessage = actions.onShowMessage,
        onCreated = { scope.launch { gridState.scrollToItem(0) } },
        origin = newWishOrigin,
    )
}

@Composable
private fun WishGrid(
    state: WishListUiState,
    gridState: LazyStaggeredGridState,
    contentPadding: PaddingValues,
    onWishClick: (String) -> Unit,
    selection: SelectionCallbacks,
) {
    val isCards = state.layout == ListLayout.CARDS
    val dragState = remember { GridDragState() }
    val currentCards by rememberUpdatedState(state.cards)
    LaunchedEffect(state.cards) { dragState.order = null }
    val cards = dragState.order ?: state.cards

    val reorderState = rememberReorderableLazyStaggeredGridState(
        lazyStaggeredGridState = gridState,
        // Прокрутка у края начинается у шапки и дока, а не у краёв экрана под ними
        scrollThresholdPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding(),
        ),
    ) { from, to -> dragState.move(currentCards, from.index, to.index) }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(if (isCards) 2 else 1),
        state = gridState,
        contentPadding = contentPadding,
        verticalItemSpacing = WishAppSpacing.s2,
        horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
        modifier = Modifier
            .fillMaxSize()
            .testTag(WishListTestTags.LIST),
    ) {
        itemsIndexed(cards, key = { _, card -> card.id }) { index, card ->
            ReorderableItem(state = reorderState, key = card.id, enabled = state.canReorder) { isDragging ->
                WishGridItem(
                    card = card,
                    position = GridPosition(index, cards.size),
                    isDragging = isDragging,
                    state = state,
                    dragState = dragState,
                    currentCards = { currentCards },
                    onWishClick = onWishClick,
                    selection = selection,
                )
            }
        }
    }
}

/** Место карточки в разделе для «Выше», «Ниже» и объявления TalkBack. */
private class GridPosition(val index: Int, val count: Int)

/**
 * Порядок, пока карточку несут, и после отпускания, пока ViewModel не отдала новый список.
 * Перенос записывается один раз при отпускании.
 */
@Stable
private class GridDragState {
    var order: List<WishCardModel>? by mutableStateOf(null)
    var hasMoved: Boolean = false

    fun start(cards: List<WishCardModel>) {
        hasMoved = false
        order = cards
    }

    fun move(cards: List<WishCardModel>, from: Int, to: Int) {
        order = (order ?: cards).toMutableList().apply { add(to, removeAt(from)) }
        hasMoved = true
    }

    /** Куда встала карточка, или `null`, если её отпустили на месте. */
    fun stop(wishId: String): Int? {
        val finalOrder = order
        return if (hasMoved && finalOrder != null) {
            finalOrder.indexOfFirst { it.id == wishId }
        } else {
            order = null
            null
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.WishGridItem(
    card: WishCardModel,
    position: GridPosition,
    isDragging: Boolean,
    state: WishListUiState,
    dragState: GridDragState,
    currentCards: () -> List<WishCardModel>,
    onWishClick: (String) -> Unit,
    selection: SelectionCallbacks,
) {
    val canReorder = state.canReorder
    val haptic = LocalHapticFeedback.current
    val itemSelection = when {
        !state.isSelectionMode -> WishSelection.NONE
        card.id in state.selectedIds -> WishSelection.SELECTED
        else -> WishSelection.UNSELECTED
    }
    // Отпускание после долгого нажатия карточка присылает как обычное нажатие, раньше конца
    // переноса. Его пропускаем: на месте выбирает перенос, а не нажатие
    val pressState = remember(card.id) { LongPressState() }
    val onClick = {
        when {
            pressState.isLongPressed -> pressState.isLongPressed = false
            state.isSelectionMode -> selection.onToggle(card.id)
            else -> onWishClick(card.id)
        }
    }
    val drag = WishItemDrag(
        longPressStartsDrag = canReorder,
        isLifted = isDragging,
        accessibilityActions = if (canReorder) moveActions(card, position, selection) else emptyList(),
    )
    // Одно долгое нажатие поднимает карточку. Повели — перенос, отпустили на месте — выбор
    val handle = if (canReorder) {
        Modifier.longPressDraggableHandle(
            onDragStarted = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                pressState.isLongPressed = true
                dragState.start(currentCards())
            },
            onDragStopped = {
                pressState.isLongPressed = false
                val toIndex = dragState.stop(card.id)
                if (toIndex != null) selection.onMoved(card.id, toIndex) else selection.onLongPress(card.id)
            },
        )
    } else {
        Modifier
    }
    if (state.layout == ListLayout.CARDS) {
        WishCard(
            model = card,
            onClick = onClick,
            onLongClick = { selection.onLongPress(card.id) },
            selection = itemSelection,
            drag = drag,
            modifier = handle,
        )
    } else {
        WishRow(
            model = card,
            onClick = onClick,
            onLongClick = { selection.onLongPress(card.id) },
            selection = itemSelection,
            drag = drag,
            modifier = handle,
        )
    }
}

/** Карточку держат долгим нажатием: отпускание не должно её открыть. */
private class LongPressState {
    var isLongPressed: Boolean = false
}

/** «Выше» и «Ниже» с объявлением новой позиции, например «Наушники, позиция 3 из 5». */
@Composable
private fun moveActions(
    card: WishCardModel,
    position: GridPosition,
    selection: SelectionCallbacks,
): List<CustomAccessibilityAction> {
    val view = LocalView.current
    val resources = LocalResources.current
    return reorderActions(index = position.index, count = position.count) { step ->
        selection.onMovedBy(card.id, step)
        val label = card.title.ifBlank { card.linkHost.orEmpty() }
        view.announceForAccessibility(
            resources.getString(R.string.a11y_moved_position, label, position.index + step + 1, position.count),
        )
    }
}

/** «Выше» и «Ниже» для TalkBack. У первой карточки нет «Выше», у последней нет «Ниже». */
@Composable
private fun reorderActions(index: Int, count: Int, onMove: (step: Int) -> Unit): List<CustomAccessibilityAction> {
    val up = stringResource(R.string.a11y_move_up)
    val down = stringResource(R.string.a11y_move_down)
    return buildList {
        if (index > 0) {
            add(
                CustomAccessibilityAction(up) {
                    onMove(-1)
                    true
                },
            )
        }
        if (index < count - 1) {
            add(
                CustomAccessibilityAction(down) {
                    onMove(1)
                    true
                },
            )
        }
    }
}

/**
 * Где стоят карточки первого экрана, пока список в самом начале: расстояние от первой карточки
 * до каждой видимой, в пикселях. Карточки выше экрана сетка не меряет, поэтому без этого плавный
 * подъём наверх шёл в два приёма: сначала на прикидку, потом добирал до точного места.
 */
private class TopLayout {
    var distances: Map<Int, Int> = emptyMap()
    var viewportHeight: Int = 0
}

@Composable
private fun rememberTopLayout(gridState: LazyStaggeredGridState): TopLayout {
    val topLayout = remember(gridState) { TopLayout() }
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo }.collect { info ->
            val isAtTop = gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0
            val firstY = info.visibleItemsInfo.firstOrNull { it.index == 0 }?.offset?.y
            if (isAtTop && firstY != null) {
                topLayout.distances = info.visibleItemsInfo.associate { it.index to it.offset.y - firstY }
                topLayout.viewportHeight = info.viewportSize.height
            }
        }
    }
    return topLayout
}

/** Забирает все касания, чтобы они не доходили до того, что под элементом. */
private fun Modifier.blockTouches(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}

/**
 * Плавно наверх одним движением по известному расстоянию. Издалека сначала прыжок к карточке
 * в нижней части первого экрана, дальше плавно: через весь список прокрутка шла бы долго.
 */
private suspend fun LazyStaggeredGridState.scrollToTopSmoothly(topLayout: TopLayout) {
    val distances = topLayout.distances
    val fromCurrent = distances[firstVisibleItemIndex]?.plus(firstVisibleItemScrollOffset)
    val distance = if (fromCurrent != null) {
        fromCurrent
    } else {
        val anchor = distances.entries
            .filter { it.key > 0 && it.value <= topLayout.viewportHeight * JUMP_ANCHOR_SHARE }
            .maxByOrNull { it.key }
        if (anchor == null) {
            // Начало списка ещё не видели, например после восстановления процесса
            animateScrollToItem(0)
            return
        }
        scrollToItem(anchor.key)
        anchor.value
    }
    if (distance > 0) animateScrollBy(-distance.toFloat(), tween(SCROLL_TO_TOP_MILLIS, easing = FastOutSlowInEasing))
    // Список мог поменяться, пока был прокручен: остаток добираем без анимации
    if (firstVisibleItemIndex != 0 || firstVisibleItemScrollOffset != 0) scrollToItem(0)
}

/** Карточка для прыжка не ниже этой доли экрана: плавная часть пути не длиннее экрана. */
private const val JUMP_ANCHOR_SHARE = 0.7f
private const val SCROLL_TO_TOP_MILLIS = 450
private const val MIN_CARDS_TO_PICK_PART = 2
