package ru.vitaliy.belyaev.wishapp.data.repository.analytics

import ru.vitaliy.belyaev.wishapp.entity.analytics.AnalyticsEvent

class AnalyticsRepositoryComposite(
    private val repositories: List<AnalyticsRepository>
) : AnalyticsRepository {

    override fun trackEvent(event: AnalyticsEvent) {
        repositories.forEach { it.trackEvent(event) }
    }
}