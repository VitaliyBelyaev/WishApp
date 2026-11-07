package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.text.Collator
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
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
import ru.vitaliy.belyaev.wishapp.utils.createSharePdfDocumentIntent
import ru.vitaliy.belyaev.wishapp.utils.createSharePlainTextIntent
import timber.log.Timber

/** Формат шеринга из меню. */
enum class ShareFormat { TEXT, PDF }

/** Разовые события для экрана. */
sealed interface WishListEvent {
    data object SharePdfFailed : WishListEvent
}

/**
 * Список нового вида: разделы «Хотелки», «Сбылось» и ярлык, сортировка, вид и поиск.
 *
 * Поиск фильтрует в памяти уже загруженный раздел по названию и комментарию. Запрос живёт здесь,
 * поэтому он сохраняется, когда открыли хотелку и вернулись.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WishListViewModel @Inject constructor(
    private val wishesRepository: WishesRepository,
    private val tagsRepository: TagsRepository,
    private val imagesRepository: ImagesRepository,
    private val dataStoreRepository: DataStoreRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(WishListUiState())
    val uiState: StateFlow<WishListUiState> = _uiState.asStateFlow()

    private val _events = Channel<WishListEvent>(capacity = Channel.BUFFERED)
    val events: Flow<WishListEvent> = _events.receiveAsFlow()

    private val _isSharingPdf = MutableStateFlow(false)
    val isSharingPdf: StateFlow<Boolean> = _isSharingPdf.asStateFlow()

    private val sectionFlow = MutableStateFlow<ListSection>(ListSection.Wishes)

    /** `null`, пока поиск закрыт. */
    private val queryFlow = MutableStateFlow<String?>(null)

    /** Хотелки раздела без поиска: их же отправляет шеринг. */
    private var sectionWishes: List<WishEntity> = emptyList()

    init {
        launchSafe { observeList() }
    }

    fun onSectionSelected(section: ListSection) {
        // Смена раздела в панели закрывает поиск
        queryFlow.value = null
        sectionFlow.value = section
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
        val wishes = sectionWishes
        val title = context.getString(R.string.wish_list_title)
        when (format) {
            ShareFormat.TEXT -> launchSafe(Dispatchers.Default) {
                val text = ShareWishListTextGenerator.generateFormattedWishListText(title = title, wishes = wishes)
                withContext(Dispatchers.Main) { context.startActivity(createSharePlainTextIntent(text)) }
            }
            ShareFormat.PDF -> launchSafe(Dispatchers.Default) {
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
                withContext(Dispatchers.Main) { context.startActivity(createSharePdfDocumentIntent(pdfFile, context)) }
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

        combine(
            sectionWishesFlow,
            tagsFlow,
            wishesRepository.observeWishesCount(isCompleted = true),
            queryFlow,
            otherSectionFlow,
        ) { data, tags, doneCount, query, otherWishes ->
            buildState(data, tags, doneCount, query, otherWishes)
        }.collect { state ->
            // Ярлык удалили, пока он был выбран: возвращаемся в «Хотелки»
            if (state.section is ListSection.Tag && state.selectedTag == null) {
                sectionFlow.value = ListSection.Wishes
                queryFlow.value = null
                return@collect
            }
            _uiState.value = state
        }
    }

    private fun buildState(
        data: SectionData,
        tags: List<TagSheetItem>,
        doneCount: Long,
        query: String?,
        otherWishes: List<WishEntity>,
    ): WishListUiState {
        val section = data.section
        val selectedTag: TagEntity? = (section as? ListSection.Tag)?.let { tagSection ->
            tags.firstOrNull { it.tag.id == tagSection.tagId }?.tag
        }
        sectionWishes = data.wishes
        val visibleWishes = if (query.isNullOrBlank()) data.wishes else data.wishes.filter { it.matches(query) }
        val search = query?.let { q ->
            SearchState(
                query = q,
                otherSectionMatches = if (q.isBlank()) 0 else otherWishes.count { it.matches(q) },
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

    private class SectionData(
        val section: ListSection,
        val settings: ListSettings,
        val wishes: List<WishEntity>,
    )
}

/** Поиск по названию и комментарию, без учёта регистра. Ссылки и ярлыки не входят. */
internal fun WishEntity.matches(query: String): Boolean {
    val q = query.trim()
    if (q.isEmpty()) return true
    return title.contains(q, ignoreCase = true) || comment.contains(q, ignoreCase = true)
}
