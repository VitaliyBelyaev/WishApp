package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.domain.WishInteractor
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.WishesRepository
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.createEmptyWish
import ru.vitaliy.belyaev.wishapp.navigation.WishDetailedRouteWithArgs.ARG_WISH_ID
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishItem

@ExperimentalCoroutinesApi
@HiltViewModel
class WishDetailedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wishesRepository: WishesRepository,
    private val wishInteractor: WishInteractor
) : ViewModel() {

    private val inputWishId: String = savedStateHandle[ARG_WISH_ID] ?: ""
    lateinit var wishId: String

    val uiState: MutableStateFlow<Optional<WishItem>> = MutableStateFlow(Optional.empty())

    init {
        viewModelScope.launch {
            wishId = if (inputWishId.isNotBlank()) {
                inputWishId
            } else {
                val wish = createEmptyWish()
                withContext(Dispatchers.IO) {
                    wishesRepository.insertWish()
                }
                wish.id
            }
            val wishItem = wishInteractor.getById(wishId)
            uiState.value = Optional.of(wishItem)

            val wishItemWithLinkPreview = wishInteractor.getLinkPreview(wishItem.wish)
            uiState.value = Optional.of(wishItemWithLinkPreview)

        }
    }

    fun onBackPressed() {
        viewModelScope.cancel()
    }

    fun onWishTitleChanged(newValue: String) {
        viewModelScope.launch {
            wishesRepository.updateTitle(newValue, wishId)
        }
    }

    fun onWishLinkChanged(newValue: String) {
        viewModelScope.launch {
            wishesRepository.updateLink(newValue, wishId)
        }
    }

    fun onWishCommentChanged(newValue: String) {
        viewModelScope.launch {
            wishesRepository.updateComment(newValue, wishId)
        }
    }

    fun onDeleteWishClicked() {
        viewModelScope.cancel()
    }
}