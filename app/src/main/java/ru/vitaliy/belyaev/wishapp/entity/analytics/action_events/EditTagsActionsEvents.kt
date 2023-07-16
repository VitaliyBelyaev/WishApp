package ru.vitaliy.belyaev.wishapp.entity.analytics.action_events

import ru.vitaliy.belyaev.wishapp.entity.analytics.AnalyticsEvent

object EditTagsRenameTagDoneClickedEvent : AnalyticsEvent {

    override val name: String = "Edit Tags Screen - Rename Tag Done Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object EditTagsDeleteTagConfirmedEvent : AnalyticsEvent {

    override val name: String = "Edit Tags Screen - Delete Tag Confirmed"

    override val params: Map<String, Any?> = emptyMap()
}