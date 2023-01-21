package ru.vitaliy.belyaev.wishapp.ui.screens.main

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
import ru.vitaliy.belyaev.wishapp.data.database.Tag
import ru.vitaliy.belyaev.wishapp.data.database.Wish
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsNames
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.tags.TagsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.wishes.WishesRepository
import ru.vitaliy.belyaev.wishapp.domain.WishesInteractor
import ru.vitaliy.belyaev.wishapp.entity.TagWithWishCount
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.MainScreenState
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.MoveDirection
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.ReorderButtonState
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.ScrollInfo
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishesFilter

@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val wishesRepository: WishesRepository,
    private val tagsRepository: TagsRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val wishesInteractor: WishesInteractor
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

    private val testWishes = createTestWishes()
    private var testWishIndex = 0

    private var scrollInfo: ScrollInfo? = null

    init {
        analyticsRepository.trackEvent(AnalyticsNames.Event.SCREEN_VIEW) {
            param(AnalyticsNames.Param.SCREEN_NAME, "MainScreen")
        }

        launchSafe {
            wishesFilterFlow
                .flatMapLatest {
                    when (it) {
                        is WishesFilter.ByTag -> {
                            wishesInteractor.observeNotCompletedWishesByTag(it.tag.tagId)
                        }
                        is WishesFilter.All -> {
                            wishesInteractor.observeNotCompletedWishes()
                        }
                        is WishesFilter.Completed -> {
                            wishesInteractor.observeCompletedWishes()
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
        }

        launchSafe {
            tagsRepository
                .observeAllTagsWithWishesCount()
                .collect { tagsWithWishesCount ->
                    _tagsWithWishCount.value = tagsWithWishesCount

                    val currentWishesFilter = wishesFilterFlow.value
                    if (currentWishesFilter is WishesFilter.ByTag) {
                        tagsWithWishesCount
                            .map { it.tag }
                            .find { it.tagId == currentWishesFilter.tag.tagId }
                            ?.let {
                                wishesFilterFlow.value = currentWishesFilter.copy(tag = it)
                            }
                    }
                }
        }

        launchSafe {
            wishesInteractor
                .observeWishesCount(isCompleted = false)
                .collect { _currentWishesCount.value = it }
        }

        launchSafe {
            wishesInteractor
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
                wishId = UUID.randomUUID().toString(),
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis
            )
            wishesRepository.insertWish(wish)
            testWishIndex++
        }
    }

    fun onCloseEditModeClicked() {
        analyticsRepository.trackEvent(AnalyticsNames.Event.CLOSE_EDIT_MODE_CLICK)
        _uiState.value = _uiState.value.copy(selectedIds = emptyList())
    }

    fun onDeleteSelectedClicked() {
        launchSafe {
            val selectedIds = uiState.value.selectedIds
            analyticsRepository.trackEvent(AnalyticsNames.Event.DELETE_FROM_EDIT_MODE_CLICK) {
                param(AnalyticsNames.Param.QUANTITY, selectedIds.size.toString())
            }
            runCatching { wishesRepository.deleteWishesByIds(selectedIds) }
                .onSuccess { _uiState.value = uiState.value.copy(selectedIds = emptyList()) }
                .onFailure { _showSnackFlow.emit(R.string.delete_wishes_error_message) }
        }
    }

    fun onSelectAllClicked() {
        analyticsRepository.trackEvent(AnalyticsNames.Event.SELECT_ALL_WISHES_PRESS)
        val selectedIds = uiState.value.wishes.map { it.id }
        _uiState.value = uiState.value.copy(selectedIds = selectedIds)
    }

    fun onWishLongPress(wish: WishWithTags) {
        analyticsRepository.trackEvent(AnalyticsNames.Event.WISH_LONG_PRESS)
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

    fun onMoveWish(movedWish: WishWithTags, moveDirection: MoveDirection, scrollOffset: Int) {
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
        }
    }

    fun onNavItemSelected(wishesFilter: WishesFilter) {
        if (wishesFilter is WishesFilter.ByTag) {
            analyticsRepository.trackEvent(AnalyticsNames.Event.FILTER_BY_TAG_CLICK)
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

    private fun createTestWishes(): List<Wish> {
        if (!BuildConfig.DEBUG) {
            return emptyList()
        }
        val currentMillis = System.currentTimeMillis()
        return listOf(
            Wish(
                wishId = "1",
                title = "Шуруповерт",
                link = "https://www.citilink.ru/product/drel-shurupovert-bosch-universaldrill-18v-akkum-patron-bystrozazhimnoi-1492081/?region_id=123062&gclid=CjwKCAiAm7OMBhAQEiwArvGi3Aom3wUbhHlBUu-9OPINzsyF9rM0Q2rBUgp1jFV68iT7IUaAoTA-1xoCzPcQAvD_BwE",
                comment = "С кейсом, чтобы были головки разные и запасной аккумулятор",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            Wish(
                wishId = "2",
                title = "Поход в SPA",
                link = "",
                comment = "Побольше массажа",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            Wish(
                wishId = "3",
                title = "Робот пылесос",
                link = "https://www.citilink.ru/product/pylesos-robot-xiaomi-mi-mop-p-chernyi-1393766/?region_id=123062&gclid=CjwKCAiAm7OMBhAQEiwArvGi3DS-H1fiWV65XGxNEcrSzE1PpsULu34hK2eZ1C235ZV3OHton6qXMBoCzrQQAvD_BwE",
                comment = "Чтобы с шерстью справлялся и умел по неровнастям ездить",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            Wish(
                wishId = "4",
                title = "Халат",
                link = "",
                comment = "Цвет не яркий",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            Wish(
                wishId = "5",
                title = "Мультитул LEATHERMAN",
                link = "https://ileatherman.ru/multitul-leatherman-wave-plus-832524-s-nejlonovym-chexlom",
                comment = "",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            )
        )
    }

    private fun createEnTestWishes(): List<Wish> {
        if (!BuildConfig.DEBUG) {
            return emptyList()
        }
        val currentMillis = System.currentTimeMillis()
        return listOf(
            Wish(
                wishId = "1",
                title = "Macbook Pro 14″",
                link = "https://www.apple.com/shop/buy-mac/macbook-pro/14-inch",
                comment = "Space Gray with 32 Gb of RAM",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            Wish(
                wishId = "2",
                title = "Garmin Forerunner 955",
                link = "https://www.garmin.com/en-US/p/777655/pn/010-02638-11",
                comment = "White",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            Wish(
                wishId = "3",
                title = "Robotic Vacuum",
                link = "",
                comment = "iRobot or Shark",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            Wish(
                wishId = "4",
                title = "Scarf",
                link = "",
                comment = "Warm, neutral shades",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            Wish(
                wishId = "5",
                title = "LEATHERMAN Multitool",
                link = "https://www.amazon.com/LEATHERMAN-Multitool-Replaceable-Spring-Action-Stainless/dp/B0B2V4N34X/ref=sr_1_2?keywords=LEATHERMAN&qid=1673963133&sr=8-2",
                comment = "",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            )
        )
    }

    private fun createTestTags(): List<Tag> {
        val tagNames = listOf(
            "Power",
            "Internet",
            "Direction",
            "Series",
            "Manufacturer",
            "Drama",
            "Efficiency",
            "Maintenance",
            "Football",
            "Moment",
            "Memory",
            "Birthday",
            "Insurance"
        )
        val tags = mutableListOf<Tag>()

        tagNames.forEach {
            val tag = Tag(UUID.randomUUID().toString(), it)
            tags.add(tag)
        }
        return tags
    }
}
