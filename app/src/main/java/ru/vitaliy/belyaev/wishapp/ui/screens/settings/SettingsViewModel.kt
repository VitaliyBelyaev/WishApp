package ru.vitaliy.belyaev.wishapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "SettingsScreen")
        }

        viewModelScope.launch {
            dataStoreRepository
                .selectedThemeFlow
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