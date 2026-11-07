package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.height
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.ListLayout
import ru.vitaliy.belyaev.wishapp.ui.components.dock.DockTab
import ru.vitaliy.belyaev.wishapp.ui.components.dock.FloatingDock
import ru.vitaliy.belyaev.wishapp.ui.components.dock.FloatingDockFade
import ru.vitaliy.belyaev.wishapp.ui.components.dock.FloatingDockHeight
import ru.vitaliy.belyaev.wishapp.ui.components.wish.card.WishCard
import ru.vitaliy.belyaev.wishapp.ui.components.wish.row.WishRow
import ru.vitaliy.belyaev.wishapp.ui.components.wish.toPaperTone
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing

/** Куда список ведёт. Пока новых экранов нет, это экраны прежнего вида. */
class WishListActions(
    val onWishClick: (wishId: String) -> Unit,
    val onAddWish: (tagId: String?) -> Unit,
    val onCreateFromSearch: (title: String) -> Unit,
    val onSettings: () -> Unit,
    val onManageTags: () -> Unit,
    val onRestore: () -> Unit,
    val onShowMessage: (String) -> Unit,
)

private enum class OpenSheet { NONE, TAGS, SORT, SHARE }

@Composable
fun WishListScreen(
    actions: WishListActions,
    viewModel: WishListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isSharingPdf by viewModel.isSharingPdf.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val pdfErrorMessage = stringResource(R.string.share_pdf_error_message)
    val currentActions by rememberUpdatedState(actions)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                WishListEvent.SharePdfFailed -> currentActions.onShowMessage(pdfErrorMessage)
            }
        }
    }

    WishListContent(
        state = state,
        isSharingPdf = isSharingPdf,
        actions = actions,
        onSectionSelected = viewModel::onSectionSelected,
        onSearchOpened = viewModel::onSearchOpened,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSearchClosed = viewModel::onSearchClosed,
        onGoToOtherSection = viewModel::onGoToOtherSectionClicked,
        onLayoutToggled = viewModel::onLayoutToggled,
        onSortSelected = viewModel::onSortSelected,
        onShare = { format -> viewModel.onShareClicked(context, format) },
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun WishListContent(
    state: WishListUiState,
    isSharingPdf: Boolean,
    actions: WishListActions,
    onSectionSelected: (ListSection) -> Unit,
    onSearchOpened: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSearchClosed: () -> Unit,
    onGoToOtherSection: () -> Unit,
    onLayoutToggled: () -> Unit,
    onSortSelected: (SectionSort) -> Unit,
    onShare: (ShareFormat) -> Unit,
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

    // У раздела, сортировки и запроса своя сетка со своей прокруткой, и они открываются с начала.
    // Одна сетка на всё рисовала первый кадр нового раздела на старой прокрутке, а карточки
    // прежнего раздела гасли поверх нового. При возврате с другого экрана прокрутка остаётся
    val listKey = "${state.section}|${state.sort}|${search != null}|${search?.query}"
    val gridState = rememberSaveable(listKey, saver = LazyStaggeredGridState.Saver) { LazyStaggeredGridState() }
    val topLayout = rememberTopLayout(gridState)

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

    val sectionTitle = when (state.section) {
        ListSection.Wishes -> stringResource(R.string.nav_wishes)
        ListSection.Done -> stringResource(R.string.nav_done)
        is ListSection.Tag -> state.selectedTag?.title.orEmpty()
    }
    val shareScope = stringResource(
        R.string.share_scope,
        sectionTitle,
        pluralStringResource(R.plurals.wishes_count, state.cards.size, state.cards.size),
    )

    val density = LocalDensity.current
    val collapsePx = with(density) { HeaderCollapseDistance.toPx() }
    val progress: () -> Float = {
        if (gridState.firstVisibleItemIndex > 0) {
            1f
        } else {
            (gridState.firstVisibleItemScrollOffset / collapsePx).coerceIn(0f, 1f)
        }
    }

    val isImeVisible = WindowInsets.isImeVisible
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
                onAddClick = { actions.onAddWish(null) },
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
                        actions.onCreateFromSearch(title)
                    },
                )
            }

            else -> key(listKey) {
                WishGrid(
                    state = state,
                    gridState = gridState,
                    contentPadding = contentPadding,
                    onWishClick = actions.onWishClick,
                )
            }
        }

        if (search != null) {
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

        AnimatedVisibility(
            visible = !isImeVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
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

        AnimatedVisibility(
            visible = !isImeVisible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            FloatingDock(
                selected = when (state.section) {
                    ListSection.Wishes -> DockTab.Wishes
                    ListSection.Done -> DockTab.Done
                    is ListSection.Tag -> state.selectedTag
                        ?.let { DockTab.Tag(it.title, it.color.toPaperTone()) }
                        ?: DockTab.Wishes
                },
                doneCount = state.doneCount,
                // Нажатие на уже открытую вкладку поднимает список наверх. У «Ярлыков» так нет:
                // вкладка открывает шторку
                onWishesClick = {
                    if (state.section == ListSection.Wishes) {
                        scope.launch { gridState.scrollToTopSmoothly(topLayout) }
                    } else {
                        onSectionSelected(ListSection.Wishes)
                    }
                },
                onDoneClick = {
                    if (state.section == ListSection.Done) {
                        scope.launch { gridState.scrollToTopSmoothly(topLayout) }
                    } else {
                        onSectionSelected(ListSection.Done)
                    }
                },
                onTagsClick = { openSheet = OpenSheet.TAGS },
                onAddClick = { actions.onAddWish(state.selectedTag?.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = WishAppSpacing.s3)
                    .padding(bottom = WishAppSpacing.s3),
            )
        }

        if (openSheet == OpenSheet.SHARE) {
            // Меню закрывается, когда PDF собран и отдан системе или не получился
            var wasSharingPdf by remember { mutableStateOf(false) }
            LaunchedEffect(isSharingPdf) {
                if (wasSharingPdf && !isSharingPdf) openSheet = OpenSheet.NONE
                wasSharingPdf = isSharingPdf
            }
            ShareMenu(
                scope = shareScope,
                isSharingPdf = isSharingPdf,
                onShareText = {
                    openSheet = OpenSheet.NONE
                    onShare(ShareFormat.TEXT)
                },
                onSharePdf = { onShare(ShareFormat.PDF) },
                onDismiss = { if (!isSharingPdf) openSheet = OpenSheet.NONE },
            )
        }
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
}

@Composable
private fun WishGrid(
    state: WishListUiState,
    gridState: LazyStaggeredGridState,
    contentPadding: PaddingValues,
    onWishClick: (String) -> Unit,
) {
    val isCards = state.layout == ListLayout.CARDS
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
        items(state.cards, key = { it.id }) { card ->
            // Долгое нажатие и режим выбора появятся в части 2.3
            if (isCards) {
                WishCard(
                    model = card,
                    onClick = { onWishClick(card.id) },
                    onLongClick = {},
                    modifier = Modifier.animateItem(),
                )
            } else {
                WishRow(
                    model = card,
                    onClick = { onWishClick(card.id) },
                    onLongClick = {},
                    modifier = Modifier.animateItem(),
                )
            }
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
            .filter { it.key > 0 && it.value <= topLayout.viewportHeight * JumpAnchorShare }
            .maxByOrNull { it.key }
        if (anchor == null) {
            // Начало списка ещё не видели, например после восстановления процесса
            animateScrollToItem(0)
            return
        }
        scrollToItem(anchor.key)
        anchor.value
    }
    if (distance > 0) animateScrollBy(-distance.toFloat(), tween(ScrollToTopMillis, easing = FastOutSlowInEasing))
    // Список мог поменяться, пока был прокручен: остаток добираем без анимации
    if (firstVisibleItemIndex != 0 || firstVisibleItemScrollOffset != 0) scrollToItem(0)
}

/** Карточка для прыжка не ниже этой доли экрана: плавная часть пути не длиннее экрана. */
private const val JumpAnchorShare = 0.7f
private const val ScrollToTopMillis = 450
