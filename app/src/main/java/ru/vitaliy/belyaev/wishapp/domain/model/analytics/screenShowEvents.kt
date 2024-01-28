package ru.vitaliy.belyaev.wishapp.domain.model.analytics

data class WishListScreenShowEvent(
    val currentWishesCount: Long,
    val completedWishesCount: Long,
    val tagsCount: Int
) : AnalyticsEvent {

    override val name: String = "Wish List Screen - Show"

    override val params: Map<String, Any?> = mapOf(
        "current_wishes_count" to currentWishesCount,
        "completed_wishes_count" to completedWishesCount,
        "tags_count" to tagsCount,
    )
}

data class WishDetailedScreenShowEvent(
    private val isNewWish: Boolean
) : AnalyticsEvent {

    override val name: String = "Wish Detailed Screen - Show"

    override val params: Map<String, Any?> = mapOf(
        "is_new_wish" to isNewWish
    )
}

object SettingsScreenShowEvent : AnalyticsEvent {

    override val name: String = "Settings Screen - Show"

    override val params: Map<String, Any?> = emptyMap()
}

object BackupScreenShowEvent : AnalyticsEvent {

    override val name: String = "Backup Screen - Show"

    override val params: Map<String, Any?> = emptyMap()
}

object EditTagsScreenShowEvent : AnalyticsEvent {

    override val name: String = "Edit Tags Screen - Show"

    override val params: Map<String, Any?> = emptyMap()
}

object AboutAppScreenShowEvent : AnalyticsEvent {

    override val name: String = "About App Screen - Show"

    override val params: Map<String, Any?> = emptyMap()
}

object PrivacyPolicyScreenShowEvent : AnalyticsEvent {

    override val name: String = "Privacy Policy Screen - Show"

    override val params: Map<String, Any?> = emptyMap()
}

object WishTagsScreenShowEvent : AnalyticsEvent {

    override val name: String = "Wish Tags Screen - Show"

    override val params: Map<String, Any?> = emptyMap()
}

data class WishImagesViewerScreenShowEvent(
    private val imagesCount: Int
) : AnalyticsEvent {

    override val name: String = "Wish Images Viewer Screen - Show"

    override val params: Map<String, Any?> = mapOf(
        "images_count" to imagesCount
    )
}
