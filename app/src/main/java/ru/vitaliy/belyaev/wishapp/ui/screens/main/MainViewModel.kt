package ru.vitaliy.belyaev.wishapp.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.model.repository.DatabaseRepository

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
}
