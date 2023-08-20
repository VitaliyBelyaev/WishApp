package ru.vitaliy.belyaev.wishapp.entity.analytics

interface AnalyticsEvent {

    val name: String

    val params: Map<String, Any?>
}