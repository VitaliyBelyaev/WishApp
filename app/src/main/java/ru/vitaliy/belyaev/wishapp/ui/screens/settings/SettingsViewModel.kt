package ru.vitaliy.belyaev.wishapp.ui.screens.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.SettingsScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.SettingDataBackupClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.SettingRateAppClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.SettingShareAppClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.SettingsSelectAppThemeClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel

/** Тема и цвет на экране настроек. */
data class SettingsState(val theme: Theme, val useWallpaperColors: Boolean)

/**
 * Настройки нового вида. Тема общая с прежним видом (`KEY_THEME`), «Как обои» есть только в новом.
 * Выбор вида живёт в `AppActivityViewModel`: от него зависит всё дерево экранов.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val analyticsRepository: AnalyticsRepository,
) : BaseViewModel() {

    /** `null`, пока не прочитано из DataStore: экран не мигает выбором по умолчанию. */
    val state: StateFlow<SettingsState?> = combine(
        dataStoreRepository.selectedThemeFlow,
        dataStoreRepository.useWallpaperColorsFlow,
        ::SettingsState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    fun trackScreenShow() {
        analyticsRepository.trackEvent(SettingsScreenShowEvent)
    }

    fun onThemeSelected(theme: Theme) {
        if (state.value?.theme == theme) return
        analyticsRepository.trackEvent(SettingsSelectAppThemeClickedEvent(theme.name))
        launchSafe { dataStoreRepository.updateSelectedTheme(theme) }
    }

    fun onWallpaperColorsSelected(useWallpaperColors: Boolean) {
        if (state.value?.useWallpaperColors == useWallpaperColors) return
        launchSafe { dataStoreRepository.updateUseWallpaperColors(useWallpaperColors) }
    }

    fun onBackupClicked() {
        analyticsRepository.trackEvent(SettingDataBackupClickedEvent)
    }

    fun onRateClicked() {
        analyticsRepository.trackEvent(SettingRateAppClickedEvent)
    }

    fun onShareAppClicked() {
        analyticsRepository.trackEvent(SettingShareAppClickedEvent)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
