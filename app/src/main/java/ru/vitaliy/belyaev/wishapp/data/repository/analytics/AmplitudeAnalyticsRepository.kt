package ru.vitaliy.belyaev.wishapp.data.repository.analytics

import javax.inject.Inject
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent

@Singleton
class AmplitudeAnalyticsRepository @Inject constructor() : AnalyticsRepository {

    override fun trackEvent(event: AnalyticsEvent) {
        AmplitudeWrapper.amplitude?.track(event.name, eventProperties = event.params)
    }
}