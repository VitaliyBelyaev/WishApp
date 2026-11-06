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

/**
 * Параметры — чтобы понять, зачем удаляют: чистят список или так отмечают
 * исполнение вместо «Сбылось». Возраст считается в целых днях от создания.
 */
data class WishListDeleteWishesConfirmedEvent(
    val wishCount: Int,
    val doneCount: Int,
    val avgAgeDays: Int,
    val maxAgeDays: Int,
    val withImagesCount: Int,
    val withLinksCount: Int,
) : AnalyticsEvent {

    override val name: String = "Wish List Screen - Delete Wishes Confirmed"

    override val params: Map<String, Any?> = mapOf(
        "wish_count" to wishCount,
        "done_count" to doneCount,
        "avg_age_days" to avgAgeDays,
        "max_age_days" to maxAgeDays,
        "with_images_count" to withImagesCount,
        "with_links_count" to withLinksCount,
    )
}

object WishListGoToBackupScreenClickedEvent : AnalyticsEvent {

    override val name: String = "Wish List Screen - Go To Backup Screen Clicked"

    override val params: Map<String, Any?> = emptyMap()
}
data class WishListSortSelectedEvent(
    val sort: String,
    val tab: String,
) : AnalyticsEvent {

    override val name: String = "Wish List Screen - Sort Selected"

    override val params: Map<String, Any?> = mapOf(
        "sort" to sort,
        "tab" to tab,
    )
}
