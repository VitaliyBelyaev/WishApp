package ru.vitaliy.belyaev.wishapp.entity.analytics.action_events

import ru.vitaliy.belyaev.wishapp.entity.analytics.AnalyticsEvent

object InAppReviewRequestedEvent : AnalyticsEvent {

    override val name: String = "InApp Review Requested"

    override val params: Map<String, Any?> = emptyMap()
}

object InAppReviewShowEvent : AnalyticsEvent {

    override val name: String = "InApp Review Show"

    override val params: Map<String, Any?> = emptyMap()
}