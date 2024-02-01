package ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events

import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent

object WishImagesViewerDeleteImageConfirmedEvent : AnalyticsEvent {

    override val name: String = "Wish Images Viewer Screen - Delete Image Confirmed"

    override val params: Map<String, Any?> = emptyMap()
}