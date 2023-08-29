package ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp.privacypolicy

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.PrivacyPolicyScreenShowEvent

@HiltViewModel
class PrivacyPolicyViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    fun trackScreenShow() {
        analyticsRepository.trackEvent(PrivacyPolicyScreenShowEvent)
    }
}