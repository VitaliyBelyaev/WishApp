package ru.vitaliy.belyaev.wishapp.data.repository.analytics

import ru.vitaliy.belyaev.wishapp.entity.analytics.AnalyticsEvent

interface AnalyticsRepository {

    fun trackEvent(event: AnalyticsEvent)
}