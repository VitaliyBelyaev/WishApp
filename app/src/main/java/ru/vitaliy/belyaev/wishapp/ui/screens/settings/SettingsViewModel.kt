package ru.vitaliy.belyaev.wishapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.model.repository.datastore.DataStoreRepository

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

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

    fun updateSelectedTheme(theme: Theme) {
        viewModelScope.launch {
            dataStoreRepository.updateSelectedTheme(theme)
        }
    }
}