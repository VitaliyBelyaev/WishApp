package ru.vitaliy.belyaev.wishapp.entity.analytics.action_events

import ru.vitaliy.belyaev.wishapp.entity.analytics.AnalyticsEvent

object AboutAppProjectUrlClickedEvent : AnalyticsEvent {

    override val name: String = "About App Screen - Project Url Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object AboutAppOpenSourceLicencesClickedEvent : AnalyticsEvent {

    override val name: String = "About App Screen - Open Source Licences Clicked"

    override val params: Map<String, Any?> = emptyMap()
}