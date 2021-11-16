package ru.vitaliy.belyaev.wishapp.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.entity.isEmpty
import ru.vitaliy.belyaev.wishapp.model.repository.DatabaseRepository

@HiltViewModel
class AppActivityViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    val wishListLiveData: MutableLiveData<List<Wish>> = MutableLiveData()

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

    fun requestWishListAsText() {
        viewModelScope.launch {
            val list = databaseRepository.getAll()
            wishListLiveData.postValue(list)
        }
    }
}