package ru.vitaliy.belyaev.wishapp.ui.screens.wish_list

import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.entity.analytics.WishListScreenShowEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.WishListDeleteWishesConfirmedEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.WishListFilterByTagClickedEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.WishListFilterCompletedClickedEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.WishListFilterCurrentClickedEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.WishListWishMovedEvent
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagWithWishCount
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.TagsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.shared.utils.SampleDataGenerator
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.MainScreenState
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.MoveDirection
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.ReorderButtonState
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.ScrollInfo
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.WishesFilter
import timber.log.Timber

@ExperimentalCoroutinesApi
@HiltViewModel
class WishListViewModel @Inject constructor(
    private val wishesRepository: WishesRepository,
    private val tagsRepository: TagsRepository,
    private val analyticsRepository: AnalyticsRepository,
) : BaseViewModel() {

    private val _uiState: MutableStateFlow<MainScreenState> = MutableStateFlow(MainScreenState())
    val uiState: StateFlow<MainScreenState> = _uiState.asStateFlow()

    private val _tagsWithWishCount: MutableStateFlow<List<TagWithWishCount>> = MutableStateFlow(emptyList())
    val tagsWithWishCount: StateFlow<List<TagWithWishCount>> = _tagsWithWishCount.asStateFlow()

    private val _currentWishesCount: MutableStateFlow<Long> = MutableStateFlow(-1)
    val currentWishesCount: StateFlow<Long> = _currentWishesCount.asStateFlow()

    private val _completedWishesCount: MutableStateFlow<Long> = MutableStateFlow(-1)
    val completedWishesCount: StateFlow<Long> = _completedWishesCount.asStateFlow()

    private val _showSnackFlow = MutableSharedFlow<Int>()
    val showSnackFlow: SharedFlow<Int> = _showSnackFlow.asSharedFlow()

    private val _scrollInfoFlow = MutableSharedFlow<ScrollInfo>()
    val scrollInfoFlow: SharedFlow<ScrollInfo> = _scrollInfoFlow.asSharedFlow()

    private val wishesFilterFlow = MutableStateFlow<WishesFilter>(WishesFilter.All)

    private val testWishes = createTestWishes(true)
    private var testWishIndex = 0

    private var scrollInfo: ScrollInfo? = null

    init {
        launchObservingWishes()

        launchSafe {
            tagsRepository
                .observeAllTagsWithWishesCount()
                .collect { tagsWithWishesCount ->
                    _tagsWithWishCount.value = tagsWithWishesCount

                    val currentWishesFilter = wishesFilterFlow.value
                    if (currentWishesFilter is WishesFilter.ByTag) {
                        tagsWithWishesCount
                            .map { it.tag }
                            .find { it.id == currentWishesFilter.tag.id }
                            ?.let {
                                wishesFilterFlow.value = currentWishesFilter.copy(tag = it)
                            }
                    }
                }
        }

        launchSafe {
            wishesRepository
                .observeWishesCount(isCompleted = false)
                .collect { _currentWishesCount.value = it }
        }

        launchSafe {
            wishesRepository
                .observeWishesCount(isCompleted = true)
                .collect { _completedWishesCount.value = it }
        }

//        if (BuildConfig.DEBUG) {
//            launchSafe {
//                val haveTags = tagsRepository.getAllTags().isNotEmpty()
//                if (!haveTags) {
//                    val testTags = createTestTags()
//                    testTags.forEach {
//                        tagsRepository.insertTag(it)
//                    }
//                }
//            }
//        }
    }

    fun trackScreenShow() {
        analyticsRepository.trackEvent(
            WishListScreenShowEvent(
                currentWishesCount = currentWishesCount.value,
                completedWishesCount = completedWishesCount.value,
                tagsCount = tagsWithWishCount.value.size
            )
        )
    }

    fun onReorderIconClicked() {
        val oldReorderButtonState = uiState.value.reorderButtonState as? ReorderButtonState.Visible ?: return
        val newIsReorderEnabled = !oldReorderButtonState.isEnabled
        val selectedIds = if (newIsReorderEnabled) {
            emptyList()
        } else {
            uiState.value.selectedIds
        }
        _uiState.value = _uiState.value.copy(
            reorderButtonState = oldReorderButtonState.copy(isEnabled = newIsReorderEnabled),
            selectedIds = selectedIds
        )
    }

    fun onAddTestWishClicked() {
        if (testWishes.isEmpty()) {
            return
        }
        launchSafe {
            val currentMillis = System.currentTimeMillis()
            val wish = testWishes[testWishIndex % testWishes.size].copy(
                id = UUID.randomUUID().toString(),
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis
            )
            wishesRepository.insertWish(wish)
            testWishIndex++
        }
    }

    fun onCloseEditModeClicked() {
        _uiState.value = _uiState.value.copy(selectedIds = emptyList())
    }

    fun onDeleteSelectedClicked() {
        launchSafe {
            val selectedIds = uiState.value.selectedIds
            analyticsRepository.trackEvent(WishListDeleteWishesConfirmedEvent(selectedIds.size))
            runCatching { wishesRepository.deleteWishesByIds(selectedIds) }
                .onSuccess { _uiState.value = uiState.value.copy(selectedIds = emptyList()) }
                .onFailure { _showSnackFlow.emit(R.string.delete_wishes_error_message) }
        }
    }

    fun onSelectAllClicked() {
        val selectedIds = uiState.value.wishes.map { it.id }
        _uiState.value = uiState.value.copy(selectedIds = selectedIds)
    }

    fun onWishLongPress(wish: WishEntity) {
        val oldState = _uiState.value
        val wishId = wish.id
        val selectedIds = if (oldState.selectedIds.isEmpty()) {
            listOf(wishId)
        } else {
            val oldSelectedIds = oldState.selectedIds.toMutableList()
            val alreadySelected = oldSelectedIds.contains(wishId)
            if (alreadySelected) {
                oldSelectedIds - wishId
            } else {
                oldSelectedIds + wishId
            }
        }
        val oldReorderButtonState = uiState.value.reorderButtonState
        val reorderButtonState = if (selectedIds.isNotEmpty() && oldReorderButtonState is ReorderButtonState.Visible) {
            oldReorderButtonState.copy(isEnabled = false)
        } else {
            oldReorderButtonState
        }
        _uiState.value = oldState.copy(selectedIds = selectedIds, reorderButtonState = reorderButtonState)
    }

    fun onMoveWish(movedWish: WishEntity, moveDirection: MoveDirection, scrollOffset: Int) {
        val wish1Index = uiState.value.wishes.indexOf(movedWish).takeIf { it != -1 } ?: return
        val wish2Index = when (moveDirection) {
            MoveDirection.UP -> wish1Index - 1
            MoveDirection.DOWN -> wish1Index + 1
        }.takeIf { it in 0..uiState.value.wishes.lastIndex } ?: return
        val wish2 = uiState.value.wishes[wish2Index]

        launchSafe {
            this.scrollInfo = ScrollInfo(wish2Index, scrollOffset)
            wishesRepository.swapWishesPositions(
                wish1Id = movedWish.id,
                wish1Position = movedWish.position,
                wish2Id = wish2.id,
                wish2Position = wish2.position
            )
            analyticsRepository.trackEvent(WishListWishMovedEvent)
        }
    }

    fun onNavItemSelected(wishesFilter: WishesFilter) {
        when (wishesFilter) {
            is WishesFilter.All -> analyticsRepository.trackEvent(WishListFilterCurrentClickedEvent)
            is WishesFilter.ByTag -> analyticsRepository.trackEvent(WishListFilterByTagClickedEvent)
            is WishesFilter.Completed -> analyticsRepository.trackEvent(WishListFilterCompletedClickedEvent)
        }
        wishesFilterFlow.value = wishesFilter

        val oldReorderButtonState = uiState.value.reorderButtonState
        val reorderButtonState = when (wishesFilter) {
            is WishesFilter.All,
            is WishesFilter.ByTag -> {
                if (oldReorderButtonState is ReorderButtonState.Visible) {
                    oldReorderButtonState
                } else {
                    ReorderButtonState.Visible(isEnabled = false)
                }
            }
            is WishesFilter.Completed -> ReorderButtonState.Hidden
        }
        _uiState.value = uiState.value.copy(reorderButtonState = reorderButtonState, selectedIds = emptyList())
    }

    private fun launchObservingWishes() {
        launchSafe {
            runCatching {
                wishesFilterFlow
                    .flatMapLatest {
                        when (it) {
                            is WishesFilter.ByTag -> {
                                wishesRepository.observeWishesByTag(it.tag.id)
                            }
                            is WishesFilter.All -> {
                                wishesRepository.observeAllWishes(isCompleted = false)
                            }
                            is WishesFilter.Completed -> {
                                wishesRepository.observeAllWishes(isCompleted = true)
                            }
                        }
                    }
                    .collect { wishes ->
                        _uiState.value = uiState.value.copy(
                            wishes = wishes,
                            wishesFilter = wishesFilterFlow.value,
                            isLoading = false
                        )
                        scrollInfo?.let {
                            _scrollInfoFlow.emit(it)
                            scrollInfo = null
                        }
                    }
            }.onFailure {
                Timber.e(it)
                wishesFilterFlow.value = WishesFilter.All
                launchObservingWishes()
            }
        }
    }

    private fun createTestWishes(forRu: Boolean): List<WishEntity> {
        if (!BuildConfig.DEBUG) {
            return emptyList()
        }
        return if (forRu) {
            SampleDataGenerator.createRuWishes()
        } else {
            SampleDataGenerator.createEnWishes()
        }
    }
}
