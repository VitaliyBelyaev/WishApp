package ru.vitaliy.belyaev.wishapp.ui.screens.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsNames
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val analyticsRepository: AnalyticsRepository
) : BaseViewModel() {

    private val _selectedTheme: MutableStateFlow<Theme> = MutableStateFlow(Theme.SYSTEM)
    val selectedTheme: StateFlow<Theme> = _selectedTheme

    init {
        analyticsRepository.trackEvent(AnalyticsNames.Event.SCREEN_VIEW) {
            param(AnalyticsNames.Param.SCREEN_NAME, "SettingsScreen")
        }

        launchSafe {
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
        launchSafe {
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