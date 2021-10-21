package ru.vitaliy.belyaev.wishapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.model.repository.DatabaseRepository
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow(emptyList<Wish>())
    val uiState: StateFlow<List<Wish>> = _uiState

    init {
        viewModelScope.launch {
            databaseRepository
                .getAll()
                .collect { wishItems -> _uiState.value = wishItems }
        }
    }


    fun onAddWishClicked() {
        val id = UUID.randomUUID().toString()
        val item = Wish(
            id,
            "Title of $id",
            "link",
            "comm",
            isCompleted = false,
            createdTimestamp = 0,
            updatedTimestamp = 0,
            tags = emptyList()
        )
        viewModelScope.launch {
            databaseRepository.insert(item)
        }
    }

    fun addItem(item: Wish) {
        databaseRepository.insert(item)
    }

    fun removeItem(item: Wish) {
        databaseRepository.deleteByIds(listOf(item.id))
    }

    fun onItemClicked(item: Wish) {

    }
}
