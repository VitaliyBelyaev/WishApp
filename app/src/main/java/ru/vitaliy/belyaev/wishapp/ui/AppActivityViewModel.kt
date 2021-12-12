package ru.vitaliy.belyaev.wishapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
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
    private val _selectedTheme: MutableStateFlow<Theme> = MutableStateFlow(Theme.SYSTEM)
    val selectedTheme: StateFlow<Theme> = _selectedTheme

    init {
        viewModelScope.launch {
            dataStoreRepository
                .selectedTheme
                .collect {
                    _selectedTheme.value = it
                }
        }
    }

    fun onWishScreenExit(wishId: String) {
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