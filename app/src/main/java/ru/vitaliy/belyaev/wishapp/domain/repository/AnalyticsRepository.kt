package ru.vitaliy.belyaev.wishapp.domain.repository

import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent

interface AnalyticsRepository {

    fun trackEvent(event: AnalyticsEvent)
}