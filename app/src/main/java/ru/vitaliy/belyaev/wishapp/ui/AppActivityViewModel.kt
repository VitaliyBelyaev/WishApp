package ru.vitaliy.belyaev.wishapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.model.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.WishesRepository
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.isEmpty
import ru.vitaliy.belyaev.wishapp.utils.SingleLiveEvent

@HiltViewModel
class AppActivityViewModel @Inject constructor(
    private val wishesRepository: WishesRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    val wishListToShareLiveData: SingleLiveEvent<List<WishWithTags>> = SingleLiveEvent()
    val requestReviewLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _selectedTheme: MutableStateFlow<Theme> = MutableStateFlow(Theme.SYSTEM)
    val selectedTheme: StateFlow<Theme> = _selectedTheme

    init {
        viewModelScope.launch {
            dataStoreRepository
                .selectedThemeFlow
                .collect {
                    _selectedTheme.value = it
                }
        }

        viewModelScope.launch {
            combine(
                dataStoreRepository.positiveActionsCountFlow,
                dataStoreRepository.reviewRequestShownCountFlow
            ) { positiveActionsCount, reviewRequestShownCount ->
                val needShowReviewRequest = positiveActionsCount != 0 &&
                        reviewRequestShownCount != positiveActionsCount &&
                        positiveActionsCount % 10 == 0
                needShowReviewRequest to positiveActionsCount
            }
                .collect { (needShowReviewRequest, positiveActionsCount) ->
                    if (needShowReviewRequest) {
                        dataStoreRepository.updateReviewRequestShownCount(positiveActionsCount)
                        requestReviewLiveData.call()
                    }
                }
        }
    }

    fun onWishScreenExit(wishId: String, isNewWish: Boolean) {
        deleteWishIfEmpty(wishId)
        if (isNewWish) {
            viewModelScope.launch { dataStoreRepository.incrementPositiveActionsCount() }
        }
    }

    private fun deleteWishIfEmpty(wishId: String) {
        viewModelScope.launch {
            val wish: WishWithTags = wishesRepository.getWishById(wishId)
            if (wish.isEmpty()) {
                wishesRepository.deleteWishesByIds(listOf(wishId))
            }
        }
    }

    fun onDeleteWishClicked(wishId: String) {
        viewModelScope.launch {
            wishesRepository.deleteWishesByIds(listOf(wishId))
        }
    }

    fun onShareWishListClicked(wishes: List<WishWithTags>) {
        wishListToShareLiveData.postValue(wishes)
    }
}