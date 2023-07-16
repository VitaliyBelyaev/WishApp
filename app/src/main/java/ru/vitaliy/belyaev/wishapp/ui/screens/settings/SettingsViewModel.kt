package ru.vitaliy.belyaev.wishapp.ui.screens.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.entity.analytics.SettingsScreenShowEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.SettingDataBackupClickedEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.SettingRateAppClickedEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.SettingShareAppClickedEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.SettingsSelectAppThemeClickedEvent
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val analyticsRepository: AnalyticsRepository
) : BaseViewModel() {

    private val _selectedTheme: MutableStateFlow<Theme> = MutableStateFlow(Theme.SYSTEM)
    val selectedTheme: StateFlow<Theme> = _selectedTheme

    init {
        launchSafe {
            dataStoreRepository
                .selectedThemeFlow
                .collect {
                    _selectedTheme.value = it
                }
        }
    }

    fun trackScreenShow() {
        analyticsRepository.trackEvent(SettingsScreenShowEvent)
    }

    fun onThemeItemClicked(theme: Theme) {
        analyticsRepository.trackEvent(SettingsSelectAppThemeClickedEvent(theme.name))
        launchSafe {
            dataStoreRepository.updateSelectedTheme(theme)
        }
    }

    fun onBackupAndRestoreItemClicked() {
        analyticsRepository.trackEvent(SettingDataBackupClickedEvent)
    }

    fun onRateAppItemClicked() {
        analyticsRepository.trackEvent(SettingRateAppClickedEvent)
    }

    fun onShareAppItemClicked() {
        analyticsRepository.trackEvent(SettingShareAppClickedEvent)
    }
}