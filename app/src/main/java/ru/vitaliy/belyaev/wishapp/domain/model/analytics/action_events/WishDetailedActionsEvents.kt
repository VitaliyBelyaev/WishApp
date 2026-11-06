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
/**
 * Новая хотелка сохранена (не пустая). Одно имя на все способы создания:
 * в редизайне шторка добавления шлёт его же с другим source.
 */
data class WishCreatedEvent(
    val source: String,
    val hasTitle: Boolean,
    val linksCount: Int,
    val imagesCount: Int,
    val hasComment: Boolean,
    val tagsCount: Int,
) : AnalyticsEvent {

    override val name: String = "Wish Created"

    override val params: Map<String, Any?> = mapOf(
        "source" to source,
        "has_title" to hasTitle,
        "links_count" to linksCount,
        "images_count" to imagesCount,
        "has_comment" to hasComment,
        "tags_count" to tagsCount,
    )

    companion object {
        const val SOURCE_WISH_SCREEN = "wish_screen"
    }
}
