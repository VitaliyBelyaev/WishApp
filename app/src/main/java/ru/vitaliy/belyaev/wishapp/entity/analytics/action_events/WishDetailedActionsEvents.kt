package ru.vitaliy.belyaev.wishapp.entity.analytics.action_events

import ru.vitaliy.belyaev.wishapp.entity.analytics.AnalyticsEvent

object WishDetailedDeleteWishConfirmedEvent : AnalyticsEvent {

    override val name: String = "Wish Detailed Screen - Delete Wish Confirmed"

    override val params: Map<String, Any?> = emptyMap()
}

object WishDetailedWishLinkClickedEvent : AnalyticsEvent {

    override val name: String = "Wish Detailed Screen - Wish Link Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object WishDetailedChangeWishCompletenessClickedEvent : AnalyticsEvent {

    override val name: String = "Wish Detailed Screen - Change Wish Completeness Clicked"

    override val params: Map<String, Any?> = emptyMap()
}