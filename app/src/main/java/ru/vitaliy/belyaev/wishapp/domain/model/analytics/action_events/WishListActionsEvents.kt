package ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events

import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent

data class WishListShareClickedEvent(
    val shareType: String,
    val wishesCount: Int,
    val totalImagesCount: Int,
): AnalyticsEvent {

    override val name: String = "Wish List Screen - Share Clicked"

    override val params: Map<String, Any?> = mapOf(
        "share_type" to shareType,
        "wishes_count" to wishesCount,
        "total_images_count" to totalImagesCount
    )
}

object WishListFilterByTagClickedEvent : AnalyticsEvent {

    override val name: String = "Wish List Screen - Filter By Tag Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object WishListFilterCompletedClickedEvent : AnalyticsEvent {

    override val name: String = "Wish List Screen - Filter Completed Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object WishListFilterCurrentClickedEvent : AnalyticsEvent {

    override val name: String = "Wish List Screen - Filter Current Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object WishListWishMovedEvent : AnalyticsEvent {

    override val name: String = "Wish List Screen - Wish Moved"

    override val params: Map<String, Any?> = emptyMap()
}

data class WishListDeleteWishesConfirmedEvent(
    val wishCount: Int
) : AnalyticsEvent {

    override val name: String = "Wish List Screen - Delete Wishes Confirmed"

    override val params: Map<String, Any?> = mapOf(
        "wish_count" to wishCount
    )
}

object WishListGoToBackupScreenClickedEvent : AnalyticsEvent {

    override val name: String = "Wish List Screen - Go To Backup Screen Clicked"

    override val params: Map<String, Any?> = emptyMap()
}