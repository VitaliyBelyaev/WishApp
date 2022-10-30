package ru.vitaliy.belyaev.wishapp.ui.screens.main

import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import org.burnoutcrew.reorderable.ItemPosition
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.database.Tag
import ru.vitaliy.belyaev.wishapp.data.database.Wish
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsNames
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.tags.TagsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.wishes.WishesRepository
import ru.vitaliy.belyaev.wishapp.domain.WishesInteractor
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.MainScreenState
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishesFilter
import timber.log.Timber

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

    private val _tags: MutableStateFlow<List<Tag>> = MutableStateFlow(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

    private val _showSnackFlow = MutableSharedFlow<Int>()
    val showSnackFlow: SharedFlow<Int> = _showSnackFlow.asSharedFlow()

    private val wishesFilterFlow = MutableStateFlow<WishesFilter>(WishesFilter.All)

    private val testWishes = createTestWishes()
    private var testWishIndex = 0

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
                .collect {
                    Timber.tag("RTRT").d("wishes:${it.joinToString(separator = "\n\n")}")
                    _uiState.value = _uiState.value.copy(wishes = it, wishesFilter = wishesFilterFlow.value)
                }
        }

        launchSafe {
            tagsRepository
                .observeAllTags()
                .collect { _tags.value = it }
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
        val newIsReorderEnabled = !_uiState.value.isReorderEnabled
        val selectedIds = if (newIsReorderEnabled) {
            emptyList()
        } else {
            uiState.value.selectedIds
        }
        _uiState.value = _uiState.value.copy(
            isReorderEnabled = newIsReorderEnabled,
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

    fun onMove(from: ItemPosition, to: ItemPosition) {
        launchSafe {
            val fromWish = uiState.value.wishes.find { it.id == from.key } ?: return@launchSafe
            val toWish = uiState.value.wishes.find { it.id == to.key } ?: return@launchSafe
            Timber.tag("RTRT").d("onMove event, from:$fromWish, to:$toWish")
            Timber.tag("RTRT").d("onMove event, from index:${from.index}, to index:${to.index}")

            val newList = _uiState.value.wishes.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            _uiState.value = _uiState.value.copy(wishes = newList)

//            wishesRepository.updatePositions(
//                fromPosition = fromWish.position,
//                fromWishId = fromWish.id,
//                toPosition = toWish.position,
//                toWishId = toWish.id
//            )
        }
    }

    fun onDragEnd(uiStartIndex: Int, uiEndIndex: Int) {
        val lastIndex = uiState.value.wishes.lastIndex
        val dataStartIndex = lastIndex - uiStartIndex
        val dataEndIndex = lastIndex - uiEndIndex
        val isMoveDown = uiEndIndex > uiStartIndex
        val movedWishId = uiState.value.wishes[uiEndIndex].id

        launchSafe {
            wishesRepository.updatePositionsOnItemMove(dataStartIndex, dataEndIndex, movedWishId, isMoveDown)
        }

        //Timber.tag("RTRT").d("onDragEnd, start:$dataStartIndex, end:$dataEndIndex")
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
        val isReorderEnabled = if (selectedIds.isNotEmpty()) {
            false
        } else {
            uiState.value.isReorderEnabled
        }
        _uiState.value = oldState.copy(selectedIds = selectedIds, isReorderEnabled = isReorderEnabled)
    }

    fun onNavItemSelected(wishesFilter: WishesFilter) {
        if (wishesFilter is WishesFilter.ByTag) {
            analyticsRepository.trackEvent(AnalyticsNames.Event.FILTER_BY_TAG_CLICK)
        }
        wishesFilterFlow.value = wishesFilter
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
