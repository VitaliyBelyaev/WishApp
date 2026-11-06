package ru.vitaliy.belyaev.wishapp.domain.repository

import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent

interface AnalyticsRepository {

    fun trackEvent(event: AnalyticsEvent)

    /** Свойства пользователя: приклеиваются ко всем следующим событиям. */
    fun setUserProperties(properties: Map<String, Any>)
}
