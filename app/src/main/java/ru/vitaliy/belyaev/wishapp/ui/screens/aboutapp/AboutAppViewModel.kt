package ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsNames
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository

@HiltViewModel
class AboutAppViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    init {
        analyticsRepository.trackEvent(AnalyticsNames.Event.SCREEN_VIEW) {
            param(AnalyticsNames.Param.SCREEN_NAME, "AboutApp")
        }
    }

    fun onSendFeedbackClicked() {
        analyticsRepository.trackEvent(AnalyticsNames.Event.SEND_FEEDBACK_CLICK)
    }

    fun onSourceCodeUrlClicked() {
        analyticsRepository.trackEvent(AnalyticsNames.Event.SOURCE_CODE_URL_CLICK)
    }
}