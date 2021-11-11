package ru.vitaliy.belyaev.wishapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.entity.isEmpty
import ru.vitaliy.belyaev.wishapp.model.repository.DatabaseRepository

@HiltViewModel
class AppActivityViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    fun onWishScreenExit(wishId: String) {
        viewModelScope.launch {
            val wish = databaseRepository.getById(wishId).firstOrNull() ?: return@launch
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
}