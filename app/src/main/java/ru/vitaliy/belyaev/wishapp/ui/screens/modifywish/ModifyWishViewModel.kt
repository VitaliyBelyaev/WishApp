package ru.vitaliy.belyaev.wishapp.ui.screens.modifywish

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Optional
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.entity.createEmptyWish
import ru.vitaliy.belyaev.wishapp.model.repository.DatabaseRepository
import ru.vitaliy.belyaev.wishapp.navigation.ModifyWishRouteWithArgs

@HiltViewModel
class ModifyWishViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val inputWishId: String = savedStateHandle[ModifyWishRouteWithArgs.ARG_WISH_ID] ?: ""
    private lateinit var wishId: String

    private val _uiState: MutableStateFlow<Optional<Wish>> = MutableStateFlow(Optional.empty())
    val uiState: StateFlow<Optional<Wish>> = _uiState

    init {
        viewModelScope.launch {
            wishId = if (inputWishId.isNotBlank()) {
                inputWishId
            } else {
                val wish = createEmptyWish()
                withContext(Dispatchers.IO) {
                    databaseRepository.insert(wish)
                }
                wish.id
            }
            databaseRepository
                .getById(wishId)
                .flowOn(Dispatchers.IO)
                .collect { wish -> _uiState.value = Optional.of(wish) }
        }
    }
}