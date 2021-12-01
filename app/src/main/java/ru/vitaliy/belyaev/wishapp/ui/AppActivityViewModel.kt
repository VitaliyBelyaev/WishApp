package ru.vitaliy.belyaev.wishapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.entity.isEmpty
import ru.vitaliy.belyaev.wishapp.model.repository.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.model.repository.DatabaseRepository
import ru.vitaliy.belyaev.wishapp.utils.SingleLiveEvent

@HiltViewModel
class AppActivityViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    val wishListToShareLiveData: SingleLiveEvent<List<Wish>> = SingleLiveEvent()
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
            val wish = databaseRepository.getByIdFlow(wishId).firstOrNull() ?: return@launch
            if (wish.isEmpty()) {
                databaseRepository.deleteByIds(listOf(wishId))
            }
        }
    }

    fun onDeleteWishClicked(wishId: String) {
        viewModelScope.launch {
            databaseRepository.deleteByIds(listOf(wishId))
        }
    }

    fun onShareWishListClicked() {
        viewModelScope.launch {
            val list = databaseRepository.getAll()
            wishListToShareLiveData.postValue(list)
        }
    }
}