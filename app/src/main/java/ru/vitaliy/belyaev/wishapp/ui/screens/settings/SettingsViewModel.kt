package ru.vitaliy.belyaev.wishapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.model.repository.analytics.AnalyticsNames
import ru.vitaliy.belyaev.wishapp.model.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.model.repository.datastore.DataStoreRepository

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _selectedTheme: MutableStateFlow<Theme> = MutableStateFlow(Theme.SYSTEM)
    val selectedTheme: StateFlow<Theme> = _selectedTheme

    init {
        analyticsRepository.trackEvent(AnalyticsNames.Event.SCREEN_VIEW) {
            param(AnalyticsNames.Param.SCREEN_NAME, "SettingsScreen")
        }

        viewModelScope.launch {
            dataStoreRepository
                .selectedThemeFlow
                .collect {
                    _selectedTheme.value = it
                }
        }
    }

    fun onThemeItemClicked(theme: Theme) {
        analyticsRepository.trackEvent(AnalyticsNames.Event.SELECT_APP_THEME) {
            param(AnalyticsNames.Param.THEME, theme.name)
        }
        viewModelScope.launch {
            dataStoreRepository.updateSelectedTheme(theme)
        }
    }

    fun onBackupAndRestoreItemClicked() {
        analyticsRepository.trackEvent(AnalyticsNames.Event.ABOUT_DATA_BACKUP_CLICK)
    }

    fun onRateAppItemClicked() {
        analyticsRepository.trackEvent(AnalyticsNames.Event.RATE_APP_CLICK)
    }
}