package ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events

import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent

object WishTagsAddNewTagClickedEvent : AnalyticsEvent {

    override val name: String = "Wish Tags Screen - Add New Tag Clicked"

    override val params: Map<String, Any?> = emptyMap()
}