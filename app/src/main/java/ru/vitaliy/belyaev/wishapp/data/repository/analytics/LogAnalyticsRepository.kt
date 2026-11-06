package ru.vitaliy.belyaev.wishapp.data.repository.analytics

import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent
import timber.log.Timber

class LogAnalyticsRepository : AnalyticsRepository {

    override fun trackEvent(event: AnalyticsEvent) {
        Timber.tag("Analytics").d("Track event: ${event.name}, params:${event.params}")
    }

    override fun setUserProperties(properties: Map<String, Any>) {
        Timber.tag("Analytics").d("Set user properties: $properties")
    }
}
