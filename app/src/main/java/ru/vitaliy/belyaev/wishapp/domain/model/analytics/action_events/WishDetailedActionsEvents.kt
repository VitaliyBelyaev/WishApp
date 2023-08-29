package ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events

import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent

object WishDetailedDeleteWishConfirmedEvent : AnalyticsEvent {

    override val name: String = "Wish Detailed Screen - Delete Wish Confirmed"

    override val params: Map<String, Any?> = emptyMap()
}

object WishDetailedChangeWishCompletenessClickedEvent : AnalyticsEvent {

    override val name: String = "Wish Detailed Screen - Change Wish Completeness Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object WishDetailedLinkClickedEvent : AnalyticsEvent {

    override val name: String = "Wish Detailed Screen - Link Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object WishDetailedAddLinkButtonClickedEvent : AnalyticsEvent {

    override val name: String = "Wish Detailed Screen - Add Link Button Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object WishDetailedDeleteLinkClickedEvent : AnalyticsEvent {

    override val name: String = "Wish Detailed Screen - Delete Link Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object WishDetailedDeleteLinkConfirmedEvent : AnalyticsEvent {

    override val name: String = "Wish Detailed Screen - Delete Link Confirmed"

    override val params: Map<String, Any?> = emptyMap()
}