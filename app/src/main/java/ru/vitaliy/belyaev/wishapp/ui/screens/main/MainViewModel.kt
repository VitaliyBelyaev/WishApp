package ru.vitaliy.belyaev.wishapp.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.domain.WishInteractor
import ru.vitaliy.belyaev.wishapp.model.repository.DatabaseRepository
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.Edit
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.MainScreenState
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.View

@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val wishInteractor: WishInteractor
) : ViewModel() {

    private val _uiState: MutableStateFlow<MainScreenState> = MutableStateFlow(MainScreenState())

    val uiState: StateFlow<MainScreenState> = _uiState

    private val testWishes = createTestWishes()

    init {
        viewModelScope.launch {
            wishInteractor
                .getWishItems()
                .collect { wishItems ->
                    val oldState = _uiState.value
                    _uiState.value = oldState.copy(wishes = wishItems)
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
            databaseRepository.insert(wish)
        }
    }

    fun onWishLongPress(wish: Wish) {
        val oldState = _uiState.value
        val wishId = wish.id
        when (oldState.mode) {
            is View -> {
                val selectedIds = listOf(wishId)
                _uiState.value = oldState.copy(mode = Edit(selectedIds))
            }
            is Edit -> {
                val oldSelectedIds = oldState.mode.selectedIds.toMutableList()
                val alreadySelected = oldSelectedIds.contains(wishId)
                val selectedIds = if (alreadySelected) {
                    oldSelectedIds - wishId
                } else {
                    oldSelectedIds + wishId
                }
                val newState = if (selectedIds.isNotEmpty()) {
                    oldState.copy(mode = Edit(selectedIds))
                } else {
                    oldState.copy(mode = View)
                }
                _uiState.value = newState
            }
        }
    }

    private fun createTestWishes(): List<Wish> {
        val currentMillis = System.currentTimeMillis()
        return listOf(
            Wish(
                id = "1",
                title = "Шуруповерт",
                link = "https://www.citilink.ru/product/drel-shurupovert-bosch-universaldrill-18v-akkum-patron-bystrozazhimnoi-1492081/?region_id=123062&gclid=CjwKCAiAm7OMBhAQEiwArvGi3Aom3wUbhHlBUu-9OPINzsyF9rM0Q2rBUgp1jFV68iT7IUaAoTA-1xoCzPcQAvD_BwE",
                comment = "Не душманский, качественный, чтобы кейс был, головки разные и запасной аккумулятор и фонарик включался когда начинаешь крутить",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                tags = emptyList()
            ),
            Wish(
                id = "2",
                title = "Айфон 13",
                link = "https://www.apple.com/ru/shop/buy-iphone/iphone-13/%D0%B4%D0%B8%D1%81%D0%BF%D0%BB%D0%B5%D0%B9-6,1-%D0%B4%D1%8E%D0%B9%D0%BC%D0%B0-128%D0%B3%D0%B1-%D1%81%D0%B8%D0%BD%D0%B8%D0%B9?afid=p238%7CsIxKMMS92-dc_mtid_187079nc38483_pcrid_546456592051_pgrid_129042575244_&cid=aos-ru-kwgo-pla-iphone--slid---product-MLP13-RU",
                comment = "Очень хочетса",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                tags = emptyList()
            ),
            Wish(
                id = "3",
                title = "Робот пылесос",
                link = "https://www.citilink.ru/product/pylesos-robot-xiaomi-mi-mop-p-chernyi-1393766/?region_id=123062&gclid=CjwKCAiAm7OMBhAQEiwArvGi3DS-H1fiWV65XGxNEcrSzE1PpsULu34hK2eZ1C235ZV3OHton6qXMBoCzrQQAvD_BwE",
                comment = "Чтобы с шерстью справлялся и умел по неровнастям ездить",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                tags = emptyList()
            ),
            Wish(
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
