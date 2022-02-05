package ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp.privacypolicy

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsNames
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository

@HiltViewModel
class PrivacyPolicyViewModel @Inject constructor(
    analyticsRepository: AnalyticsRepository
) : ViewModel() {

    init {
        analyticsRepository.trackEvent(AnalyticsNames.Event.SCREEN_VIEW) {
            param(AnalyticsNames.Param.SCREEN_NAME, "PrivacyPolicy")
        }
    }
}