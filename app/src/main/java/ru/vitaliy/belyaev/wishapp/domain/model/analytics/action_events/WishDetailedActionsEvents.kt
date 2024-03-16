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

object WishDetailedAddImagesClickedEvent : AnalyticsEvent {

    override val name: String = "Wish Detailed Screen - Add Images Button Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

data class WishDetailedImagesSelectedEvent(
    private val imagesCount: Int
) : AnalyticsEvent {

    override val name: String = "Wish Detailed Screen - Images Selected"

    override val params: Map<String, Any?> = mapOf(
        "images_count" to imagesCount
    )
}

object WishDetailedSaveAndExitClickedEvent : AnalyticsEvent {

    override val name: String = "Wish Detailed Screen - Save And Exit Clicked"

    override val params: Map<String, Any?> = emptyMap()
}