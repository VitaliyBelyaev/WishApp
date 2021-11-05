package ru.vitaliy.belyaev.wishapp.ui.screens.modifywish

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Optional
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.model.repository.DatabaseRepository
import ru.vitaliy.belyaev.wishapp.navigation.ModifyWishRouteWithArgs

@HiltViewModel
class ModifyWishViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val wishId: String = savedStateHandle[ModifyWishRouteWithArgs.ARG_WISH_ID] ?: ""

    private val _uiState: MutableStateFlow<Optional<Wish>> = MutableStateFlow(Optional.empty())
    val uiState: StateFlow<Optional<Wish>> = _uiState

    init {
        viewModelScope.launch {
            if (wishId.isNotBlank()) {
                databaseRepository
                    .getById(wishId)
                    .collect { wish -> _uiState.value = Optional.of(wish) }
            }
        }
    }
}