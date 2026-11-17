package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.text.Collator
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.model.DoneSortMode
import ru.vitaliy.belyaev.wishapp.domain.model.ListLayout
import ru.vitaliy.belyaev.wishapp.shared.domain.ShareWishListTextGenerator
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishSortMode
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.TagsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.ui.components.wish.toWishCardModel
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.utils.ShareWishListPdfGenerator
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DispatcherProvider
import ru.vitaliy.belyaev.wishapp.utils.createSharePdfDocumentIntent
import ru.vitaliy.belyaev.wishapp.utils.createSharePlainTextIntent
import timber.log.Timber

/** Формат шеринга из меню. */
enum class ShareFormat { TEXT, PDF }

/** Разовые события для экрана. */
sealed interface WishListEvent {
    data object SharePdfFailed : WishListEvent

    /**
     * Снэкбар с «Вернуть». [onUndo] возвращает хотелки в раздел, [onConfirm] записывает действие
     * в базу, когда снэкбар закрылся сам или его сменило другое сообщение.
     */
    class Undoable(
        val action: BulkAction,
        val count: Int,
        val onUndo: () -> Unit,
        val onConfirm: () -> Unit,
    ) : WishListEvent
}

/**
 * Список нового вида: разделы «Хотелки», «Сбылось» и ярлык, сортировка, вид и поиск.
 *
 * Поиск фильтрует в памяти уже загруженный раздел по названию и комментарию. Запрос живёт здесь,
 * поэтому он сохраняется, когда открыли хотелку и вернулись.
 */
// Обработчики экрана списка: разделы, поиск, вид, сортировка, шеринг, режим выбора и перенос
@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WishListViewModel @Inject constructor(
    private val wishesRepository: WishesRepository,
    private val tagsRepository: TagsRepository,
    private val imagesRepository: ImagesRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val dispatcherProvider: DispatcherProvider,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(WishListUiState())
    val uiState: StateFlow<WishListUiState> = _uiState.asStateFlow()

    private val _events = Channel<WishListEvent>(capacity = Channel.BUFFERED)
    val events: Flow<WishListEvent> = _events.receiveAsFlow()

    private val _isSharingPdf = MutableStateFlow(false)
    val isSharingPdf: StateFlow<Boolean> = _isSharingPdf.asStateFlow()

    private val sectionFlow = MutableStateFlow<ListSection>(ListSection.Wishes)

    /**
     * Раздел, выбранный в доке, сразу по нажатию. Список приходит из базы позже, и если ждать его,
     * вкладка сначала отыгрывала нажатие и только потом заливалась цветом: выглядело как мигание.
     */
    val dockSection: StateFlow<ListSection> = sectionFlow.asStateFlow()

    /** `null`, пока поиск закрыт. */
    private val queryFlow = MutableStateFlow<String?>(null)

    private val selectedIdsFlow = MutableStateFlow<Set<String>>(emptySet())

    /** Хотелки, ушедшие из раздела по массовому действию, пока снэкбар даёт его отменить. */
    private val hiddenIdsFlow = MutableStateFlow<Set<String>>(emptySet())

    /**
     * Массовые «Сбылось» и «Снова хочу», которые ещё можно отменить. Их записываем раньше снэкбара,
     * если человек сам ушёл в другой раздел: иначе он не нашёл бы там только что отмеченные.
     */
    private val pendingMoves = mutableListOf<PendingBulk>()

    /** Порядок после переноса до того, как его вернёт база: без него карточка на миг прыгала назад. */
    private val movedWishesFlow = MutableStateFlow<List<WishEntity>?>(null)
    private var lastDbWishes: List<WishEntity>? = null

    /** Хотелки раздела без поиска и без скрытых: их же отправляет шеринг и по ним считается перенос. */
    private var sectionWishes: List<WishEntity> = emptyList()

    init {
        launchSafe { observeList() }
    }

    fun onSectionSelected(section: ListSection) {
        // Смена раздела в панели закрывает поиск и снимает выбор
        queryFlow.value = null
        selectedIdsFlow.value = emptySet()
        commitPendingMoves()
        sectionFlow.value = section
    }

    /** Нажатие в режиме выбора переключает карточку. Сняли последнюю — режим закрывается. */
    fun onWishToggled(wishId: String) {
        selectedIdsFlow.update { ids -> if (wishId in ids) ids - wishId else ids + wishId }
    }

    /** Долгое нажатие без переноса выбирает карточку и открывает режим выбора. */
    fun onWishLongPressed(wishId: String) {
        selectedIdsFlow.update { it + wishId }
    }

    fun onSelectAllClicked() {
        selectedIdsFlow.value = uiState.value.cards.mapTo(mutableSetOf()) { it.id }
    }

    fun onSelectionCleared() {
        selectedIdsFlow.value = emptySet()
    }

    /**
     * Карточку перенесли на место [toIndex] в разделе. В базу пишутся только пройденные хотелки
     * раздела: в ярлыке это хотелки ярлыка, остальные позиции не трогаются.
     */
    fun onWishMoved(wishId: String, toIndex: Int) {
        val wishes = sectionWishes
        val fromIndex = wishes.indexOfFirst { it.id == wishId }
        if (fromIndex < 0 || toIndex !in wishes.indices || fromIndex == toIndex) return
        val moved = wishes[fromIndex]
        val passed = if (toIndex > fromIndex) {
            wishes.subList(fromIndex + 1, toIndex + 1).toList()
        } else {
            wishes.subList(toIndex, fromIndex).asReversed().toList()
        }
        movedWishesFlow.value = wishes.withMove(fromIndex, toIndex, passed)
        launchSafe {
            wishesRepository.swapMovedWishPositionWithPassedWishes(
                wishId = moved.id,
                wishPosition = moved.position,
                passedWishes = passed,
            )
        }
    }

    /** «Выше» и «Ниже» для TalkBack: перенос на одну карточку. */
    fun onWishMovedBy(wishId: String, step: Int) {
        val index = sectionWishes.indexOfFirst { it.id == wishId }
        if (index >= 0) onWishMoved(wishId, index + step)
    }

    fun onBulkActionClicked(action: BulkAction) {
        val ids = selectedIdsFlow.value
        if (ids.isEmpty()) return
        selectedIdsFlow.value = emptySet()
        hiddenIdsFlow.update { it + ids }
        val pending = PendingBulk(action, ids)
        if (action != BulkAction.DELETE) pendingMoves += pending
        launchSafe {
            _events.send(
                WishListEvent.Undoable(
                    action = action,
                    count = ids.size,
                    onUndo = { undoBulk(pending) },
                    onConfirm = { commitBulk(pending) },
                ),
            )
        }
    }

    private fun commitBulk(pending: PendingBulk) {
        if (pending.isCommitted) return
        pending.isCommitted = true
        pendingMoves -= pending
        launchSafe {
            try {
                applyBulkAction(pending.action, pending.ids.toList())
            } finally {
                // База уже без этих хотелок в разделе, прятать их больше не нужно
                hiddenIdsFlow.update { it - pending.ids }
            }
        }
    }

    private fun undoBulk(pending: PendingBulk) {
        pendingMoves -= pending
        if (!pending.isCommitted) {
            pending.isCommitted = true
            hiddenIdsFlow.update { it - pending.ids }
            return
        }
        // Уже записано, потому что ушли в другой раздел: возвращаем обратным действием.
        // Место в «Моём порядке» при этом не восстанавливается, хотелка встаёт наверх
        val reverse = when (pending.action) {
            BulkAction.CAME_TRUE -> BulkAction.WISH_AGAIN
            BulkAction.WISH_AGAIN -> BulkAction.CAME_TRUE
            BulkAction.DELETE -> return
        }
        launchSafe { applyBulkAction(reverse, pending.ids.toList()) }
    }

    private fun commitPendingMoves() {
        pendingMoves.toList().forEach(::commitBulk)
    }

    private suspend fun applyBulkAction(action: BulkAction, ids: List<String>) {
        when (action) {
            BulkAction.DELETE -> wishesRepository.deleteWishesByIds(ids)
            BulkAction.CAME_TRUE -> ids.forEach { wishesRepository.updateWishIsCompleted(newValue = true, wishId = it) }
            BulkAction.WISH_AGAIN -> ids.forEach {
                wishesRepository.updateWishIsCompleted(newValue = false, wishId = it)
            }
        }
    }

    fun onSearchOpened() {
        if (queryFlow.value == null) {
            queryFlow.value = ""
        }
    }

    fun onSearchQueryChanged(query: String) {
        queryFlow.value = query
    }

    fun onSearchClosed() {
        queryFlow.value = null
    }

    /** Кнопка «В «Сбылось»» в пустом результате: тот же запрос в соседнем разделе. */
    fun onGoToOtherSectionClicked() {
        val query = queryFlow.value
        commitPendingMoves()
        sectionFlow.value = otherSection(sectionFlow.value)
        queryFlow.value = query
    }

    fun onLayoutToggled() {
        val newLayout = when (uiState.value.layout) {
            ListLayout.CARDS -> ListLayout.LIST
            ListLayout.LIST -> ListLayout.CARDS
        }
        launchSafe { dataStoreRepository.updateListLayout(newLayout) }
    }

    fun onSortSelected(sort: SectionSort) {
        launchSafe {
            when (sort) {
                is SectionSort.Main -> dataStoreRepository.updateSelectedSortMode(sort.mode)
                is SectionSort.Done -> dataStoreRepository.updateDoneSortMode(sort.mode)
            }
        }
    }

    fun onShareClicked(context: Context, format: ShareFormat) {
        // В режиме выбора отправляются выбранные, в порядке раздела
        val selectedIds = selectedIdsFlow.value
        val wishes = if (selectedIds.isEmpty()) sectionWishes else sectionWishes.filter { it.id in selectedIds }
        val title = context.getString(R.string.wish_list_title)
        when (format) {
            ShareFormat.TEXT -> launchSafe(dispatcherProvider.default()) {
                val text = ShareWishListTextGenerator.generateFormattedWishListText(title = title, wishes = wishes)
                withContext(dispatcherProvider.main()) { context.startActivity(createSharePlainTextIntent(text)) }
            }
            ShareFormat.PDF -> launchSafe(dispatcherProvider.default()) {
                _isSharingPdf.value = true
                val pdfFile: File = try {
                    ShareWishListPdfGenerator.generatePdfForWishList(
                        titleResId = R.string.wish_list_title,
                        wishes = wishes,
                        imagesRepository = imagesRepository,
                        context = context,
                    )
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Timber.e(error)
                    FirebaseCrashlytics.getInstance().recordException(error)
                    _events.send(WishListEvent.SharePdfFailed)
                    return@launchSafe
                } finally {
                    _isSharingPdf.value = false
                }
                withContext(dispatcherProvider.main()) {
                    context.startActivity(createSharePdfDocumentIntent(pdfFile, context))
                }
            }
        }
    }

    private suspend fun observeList() {
        val tagsFlow = tagsRepository.observeAllTagsWithWishesCount().map { tags ->
            val collator = Collator.getInstance()
            tags.map { TagSheetItem(it.tag, it.wishesCount) }.sortedWith(compareBy(collator) { it.tag.title })
        }
        val settingsFlow = combine(
            dataStoreRepository.selectedSortModeFlow,
            dataStoreRepository.doneSortModeFlow,
            dataStoreRepository.listLayoutFlow,
        ) { mainSort, doneSort, layout -> ListSettings(mainSort, doneSort, layout) }
            .distinctUntilChanged()

        val sectionWishesFlow = combine(sectionFlow, settingsFlow) { section, settings -> section to settings }
            .distinctUntilChanged()
            .flatMapLatest { (section, settings) ->
                observeSectionWishes(section, settings).map { wishes -> SectionData(section, settings, wishes) }
            }

        // Соседний раздел нужен только для пустого результата поиска, без поиска не подписываемся
        val otherSectionFlow = combine(sectionFlow, queryFlow.map { it != null }.distinctUntilChanged()) { s, open ->
            s to open
        }.flatMapLatest { (section, isSearchOpen) ->
            if (isSearchOpen) observeOtherSectionWishes(section) else flowOf(emptyList())
        }

        val searchFlow = combine(queryFlow, otherSectionFlow) { query, otherWishes -> SearchInput(query, otherWishes) }
        val localFlow = combine(selectedIdsFlow, hiddenIdsFlow, movedWishesFlow) { selected, hidden, moved ->
            LocalState(selected, hidden, moved)
        }

        combine(
            sectionWishesFlow,
            tagsFlow,
            wishesRepository.observeWishesCount(isCompleted = true),
            searchFlow,
            localFlow,
        ) { data, tags, doneCount, searchInput, local ->
            buildState(data, tags, doneCount, searchInput, local)
        }.collect { state ->
            // Ярлык удалили, пока он был выбран: возвращаемся в «Хотелки»
            if (state.section is ListSection.Tag && state.selectedTag == null) {
                sectionFlow.value = ListSection.Wishes
                queryFlow.value = null
                selectedIdsFlow.value = emptySet()
                return@collect
            }
            _uiState.value = state
        }
    }

    private fun buildState(
        data: SectionData,
        tags: List<TagSheetItem>,
        doneCount: Long,
        searchInput: SearchInput,
        local: LocalState,
    ): WishListUiState {
        val query = searchInput.query
        val section = data.section
        val selectedTag: TagEntity? = (section as? ListSection.Tag)?.let { tagSection ->
            tags.firstOrNull { it.tag.id == tagSection.tagId }?.tag
        }
        // Новый список из базы уже с записанным порядком: свой порядок после переноса больше не нужен
        if (data.wishes !== lastDbWishes) {
            lastDbWishes = data.wishes
            movedWishesFlow.value = null
        }
        val wishes = (local.movedWishes ?: data.wishes).filterNot { it.id in local.hiddenIds }
        sectionWishes = wishes
        val visibleWishes = if (query.isNullOrBlank()) wishes else wishes.filter { it.matches(query) }
        val search = query?.let { q ->
            SearchState(
                query = q,
                otherSectionMatches = if (q.isBlank()) 0 else searchInput.otherWishes.count { it.matches(q) },
            )
        }
        return WishListUiState(
            section = section,
            selectedTag = selectedTag,
            cards = visibleWishes.map { it.toWishCardModel(filterTag = selectedTag) },
            isLoading = false,
            layout = data.settings.layout,
            sort = when (section) {
                ListSection.Done -> SectionSort.Done(data.settings.doneSort)
                else -> SectionSort.Main(data.settings.mainSort)
            },
            doneCount = doneCount,
            tags = tags,
            search = search,
            // Выбор живёт, пока хотелка в разделе: удалённые и ушедшие в другой раздел выпадают
            selectedIds = local.selectedIds.filterTo(mutableSetOf()) { id -> wishes.any { it.id == id } },
        )
    }

    private fun observeSectionWishes(section: ListSection, settings: ListSettings): Flow<List<WishEntity>> {
        return when (section) {
            ListSection.Wishes -> wishesRepository.observeAllWishesSorted(
                isCompleted = false,
                sortMode = settings.mainSort,
            )
            is ListSection.Tag -> wishesRepository.observeWishesByTagSorted(section.tagId, settings.mainSort)
            ListSection.Done -> when (settings.doneSort) {
                DoneSortMode.NEWEST -> wishesRepository.observeCompletedWishesByCompletionDate(newestFirst = true)
                DoneSortMode.OLDEST -> wishesRepository.observeCompletedWishesByCompletionDate(newestFirst = false)
                DoneSortMode.TITLE_AZ -> wishesRepository.observeAllWishesSorted(true, WishSortMode.TitleAZ)
                DoneSortMode.TITLE_ZA -> wishesRepository.observeAllWishesSorted(true, WishSortMode.TitleZA)
            }
        }
    }

    private fun observeOtherSectionWishes(section: ListSection): Flow<List<WishEntity>> {
        val isCompleted = otherSection(section) == ListSection.Done
        return wishesRepository.observeAllWishes(isCompleted = isCompleted)
    }

    private fun otherSection(section: ListSection): ListSection = when (section) {
        ListSection.Done -> ListSection.Wishes
        else -> ListSection.Done
    }

    private data class ListSettings(
        val mainSort: WishSortMode,
        val doneSort: DoneSortMode,
        val layout: ListLayout,
    )

    /** Запрос, `null` пока поиск закрыт, и хотелки соседнего раздела для пустого результата. */
    private class SearchInput(val query: String?, val otherWishes: List<WishEntity>)

    private class PendingBulk(val action: BulkAction, val ids: Set<String>) {
        var isCommitted: Boolean = false
    }

    private class LocalState(
        val selectedIds: Set<String>,
        val hiddenIds: Set<String>,
        val movedWishes: List<WishEntity>?,
    )

    private class SectionData(
        val section: ListSection,
        val settings: ListSettings,
        val wishes: List<WishEntity>,
    )
}

/**
 * Раздел после переноса карточки с [fromIndex] на [toIndex], с позициями как после
 * `swapMovedWishPositionWithPassedWishes`: каждая пройденная хотелка берёт позицию предыдущей,
 * перенесённая берёт позицию последней пройденной.
 */
internal fun List<WishEntity>.withMove(fromIndex: Int, toIndex: Int, passed: List<WishEntity>): List<WishEntity> {
    val moved = this[fromIndex]
    val newPositions = HashMap<String, Long>()
    var previousPosition = moved.position
    passed.forEach { wish ->
        newPositions[wish.id] = previousPosition
        previousPosition = wish.position
    }
    newPositions[moved.id] = previousPosition
    val reordered = toMutableList()
    reordered.add(toIndex, reordered.removeAt(fromIndex))
    return reordered.map { wish -> newPositions[wish.id]?.let { wish.copy(position = it) } ?: wish }
}

/** Поиск по названию и комментарию, без учёта регистра. Ссылки и ярлыки не входят. */
internal fun WishEntity.matches(query: String): Boolean {
    val q = query.trim()
    if (q.isEmpty()) return true
    return title.contains(q, ignoreCase = true) || comment.contains(q, ignoreCase = true)
}
