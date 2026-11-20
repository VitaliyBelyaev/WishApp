package ru.vitaliy.belyaev.wishapp.ui.screens.about

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AboutAppScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.PrivacyPolicyScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.AboutAppOpenSourceLicencesClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.AboutAppProjectUrlClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel

/** «О приложении»: только аналитика, события те же, что у прежнего экрана. */
@HiltViewModel
class AboutViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
) : BaseViewModel() {

    fun trackScreenShow() = analyticsRepository.trackEvent(AboutAppScreenShowEvent)

    fun onSourceCodeClicked() = analyticsRepository.trackEvent(AboutAppProjectUrlClickedEvent)

    fun onLicensesClicked() = analyticsRepository.trackEvent(AboutAppOpenSourceLicencesClickedEvent)
}

@HiltViewModel
class PolicyViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
) : BaseViewModel() {

    fun trackScreenShow() = analyticsRepository.trackEvent(PrivacyPolicyScreenShowEvent)
}

/** Список лицензий и текст одной лицензии: файлы читаются один раз за процесс. */
@HiltViewModel
class LicensesViewModel @Inject constructor(
    private val licenses: OpenSourceLicenses,
) : BaseViewModel() {

    private val _state = MutableStateFlow<List<OpenSourceLicense>?>(null)

    /** `null`, пока файлы не прочитаны. */
    val state: StateFlow<List<OpenSourceLicense>?> = _state.asStateFlow()

    init {
        launchSafe { _state.value = licenses.all() }
    }
}
