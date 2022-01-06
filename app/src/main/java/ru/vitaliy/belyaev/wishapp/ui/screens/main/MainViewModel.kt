package ru.vitaliy.belyaev.wishapp.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.model.repository.tags.TagsRepository
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.WishesRepository
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.AllTagsMenuItem
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.MainScreenState
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.NavigationMenuItem
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.TagMenuItem

@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val wishesRepository: WishesRepository,
    private val tagsRepository: TagsRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<MainScreenState> = MutableStateFlow(MainScreenState())
    val uiState: StateFlow<MainScreenState> = _uiState

    private val _navigationMenuUiState: MutableStateFlow<List<NavigationMenuItem>> = MutableStateFlow(emptyList())
    val navigationMenuUiState: StateFlow<List<NavigationMenuItem>> = _navigationMenuUiState

    private val selectedTagIdFlow: MutableStateFlow<String> = MutableStateFlow("")

    private val testWishes = createRuTestWishes()

    private var allWishesJob: Job? = null
    private var wishesByTagJob: Job? = null

    init {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "MainScreen")
        }

        allWishesJob = viewModelScope.launch {
            wishesRepository
                .observeAllWishes()
                .collect { wishItems -> _uiState.value = MainScreenState(wishes = wishItems) }
        }

        viewModelScope.launch {
            tagsRepository
                .observeAllTags()
                .combine(selectedTagIdFlow) { tags, selectedTagId ->
                    val navMenuItems = mutableListOf<NavigationMenuItem>().apply {
                        add(AllTagsMenuItem(R.string.all_wishes, selectedTagId.isBlank()))
                        val tagMenuItems = tags.map { TagMenuItem(it, it.tagId == selectedTagId) }
                        addAll(tagMenuItems)
                    }
                    navMenuItems.toList()
                }
                .collect {
                    _navigationMenuUiState.value = it
                }
        }
    }

    fun onAddTestWishClicked() {
        viewModelScope.launch {
            val currentMillis = System.currentTimeMillis()
            val index = Random.nextInt(0, testWishes.lastIndex)
            val wish = testWishes[index].copy(
                id = UUID.randomUUID().toString(),
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis
            )
            wishesRepository.insertWish(wish)
        }
    }

    fun onCloseEditModeClicked() {
        Firebase.analytics.logEvent("close_edit_mode_click", null)
        _uiState.value = _uiState.value.copy(selectedIds = emptyList())
    }

    fun onDeleteSelectedClicked() {
        viewModelScope.launch {
            val selectedIds = uiState.value.selectedIds
            Firebase.analytics.logEvent("delete_from_edit_mode_click") {
                param(FirebaseAnalytics.Param.QUANTITY, selectedIds.size.toString())
            }
            wishesRepository.deleteWishesByIds(selectedIds)
        }
    }

    fun onSelectAllClicked() {
        Firebase.analytics.logEvent("select_all_wishes_press", null)
        val selectedIds = uiState.value.wishes.map { it.id }
        _uiState.value = uiState.value.copy(selectedIds = selectedIds)
    }

    fun onWishLongPress(wish: WishWithTags) {
        Firebase.analytics.logEvent("wish_long_press", null)
        val oldState = _uiState.value
        val wishId = wish.id
        val newState = if (oldState.selectedIds.isEmpty()) {
            val selectedIds = listOf(wishId)
            oldState.copy(selectedIds = selectedIds)
        } else {
            val oldSelectedIds = oldState.selectedIds.toMutableList()
            val alreadySelected = oldSelectedIds.contains(wishId)
            val selectedIds = if (alreadySelected) {
                oldSelectedIds - wishId
            } else {
                oldSelectedIds + wishId
            }
            oldState.copy(selectedIds = selectedIds)
        }
        _uiState.value = newState
    }

    fun onNavItemSelected(tag: Tag?) {
        allWishesJob?.cancel()
        wishesByTagJob?.cancel()
        selectedTagIdFlow.value = tag?.tagId ?: ""
        if (tag == null) {
            allWishesJob = viewModelScope.launch {
                wishesRepository
                    .observeAllWishes()
                    .collect { wishItems ->
                        _uiState.value = MainScreenState(wishes = wishItems)
                    }
            }
        } else {
            Firebase.analytics.logEvent("filter_by_tag", null)
            wishesByTagJob = viewModelScope.launch {
                wishesRepository
                    .observeWishesByTag(tag.tagId)
                    .collect { wishItems ->
                        _uiState.value = MainScreenState(wishes = wishItems, selectedTag = tag)
                    }
            }
        }
    }

    private fun createRuTestWishes(): List<WishWithTags> {
        val currentMillis = System.currentTimeMillis()
        return listOf(
            WishWithTags(
                id = "1",
                title = "Шуруповерт",
                link = "https://www.citilink.ru/product/drel-shurupovert-bosch-universaldrill-18v-akkum-patron-bystrozazhimnoi-1492081/?region_id=123062&gclid=CjwKCAiAm7OMBhAQEiwArvGi3Aom3wUbhHlBUu-9OPINzsyF9rM0Q2rBUgp1jFV68iT7IUaAoTA-1xoCzPcQAvD_BwE",
                comment = "Не душманский, качественный, чтобы кейс был, головки разные и запасной аккумулятор и фонарик включался когда начинаешь крутить",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                tags = emptyList()
            ),
            WishWithTags(
                id = "2",
                title = "Айфон 13",
                link = "https://www.apple.com/ru/shop/buy-iphone/iphone-13/%D0%B4%D0%B8%D1%81%D0%BF%D0%BB%D0%B5%D0%B9-6,1-%D0%B4%D1%8E%D0%B9%D0%BC%D0%B0-128%D0%B3%D0%B1-%D1%81%D0%B8%D0%BD%D0%B8%D0%B9?afid=p238%7CsIxKMMS92-dc_mtid_187079nc38483_pcrid_546456592051_pgrid_129042575244_&cid=aos-ru-kwgo-pla-iphone--slid---product-MLP13-RU",
                comment = "Очень хочетса",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                tags = emptyList()
            ),
            WishWithTags(
                id = "3",
                title = "Робот пылесос",
                link = "https://www.citilink.ru/product/pylesos-robot-xiaomi-mi-mop-p-chernyi-1393766/?region_id=123062&gclid=CjwKCAiAm7OMBhAQEiwArvGi3DS-H1fiWV65XGxNEcrSzE1PpsULu34hK2eZ1C235ZV3OHton6qXMBoCzrQQAvD_BwE",
                comment = "Чтобы с шерстью справлялся и умел по неровнастям ездить",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                tags = emptyList()
            ),
            WishWithTags(
                id = "4",
                title = "Халат теплый и нежный",
                link = "",
                comment = "Цвет не яркий, спокойный какой-нибудь",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                tags = emptyList()
            )
        )
    }
}
