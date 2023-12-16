package ru.vitaliy.belyaev.wishapp.domain.model.analytics

interface AnalyticsEvent {

    val name: String

    val params: Map<String, Any?>
}