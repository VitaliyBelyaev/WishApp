package ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AboutAppScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.AboutAppOpenSourceLicencesClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.AboutAppProjectUrlClickedEvent
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel

@HiltViewModel
class AboutAppViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : BaseViewModel() {

    fun trackScreenShow() {
        analyticsRepository.trackEvent(AboutAppScreenShowEvent)
    }

    fun onSourceCodeUrlClicked() {
        analyticsRepository.trackEvent(AboutAppProjectUrlClickedEvent)
    }

    fun onOpenSourceLicencesClicked() {
        analyticsRepository.trackEvent(AboutAppOpenSourceLicencesClickedEvent)
    }
}