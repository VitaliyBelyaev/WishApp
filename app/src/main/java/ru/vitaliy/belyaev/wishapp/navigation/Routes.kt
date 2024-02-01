package ru.vitaliy.belyaev.wishapp.navigation

const val ARG_WISH_ID = "wishId"
const val ARG_WISH_LINK = "wishLink"
const val ARG_WISH_IMAGE_ID = "wishImageId"
const val ARG_WISH_IMAGE_INDEX = "wishImageIndex"

object MainRoute {
    const val VALUE = "main"
}

object WishDetailedRoute {

    const val VALUE = "wish_detailed?$ARG_WISH_ID={$ARG_WISH_ID}&$ARG_WISH_LINK={$ARG_WISH_LINK}"

    fun buildRoute(
        wishId: String? = null,
        wishLink: String? = null
    ): String {
        return StringBuilder().apply {
            append("wish_detailed")
            var separator = "?"

            if (!wishId.isNullOrBlank()) {
                append("$separator$ARG_WISH_ID=$wishId")
                separator = "&"
            }

            if (!wishLink.isNullOrBlank()) {
                append("$separator$ARG_WISH_LINK=$wishLink")
            }
        }.toString()
    }
}

object SettingsRoute {
    const val VALUE = "settings"
}

object BackupAndRestoreRoute {
    const val VALUE = "backup_and_restore"
}

object AboutAppRoute {
    const val VALUE = "about_app"
}

object PrivacyPolicyRoute {
    const val VALUE = "privacy_policy"
}

object EditTagRoute {
    const val VALUE = "edit_tag"
}

object WishTagsRoute {
    const val VALUE = "wish_tags/{$ARG_WISH_ID}"

    fun build(wishId: String): String {
        return "wish_tags/$wishId"
    }
}

object WishImagesViewerRoute {
    const val VALUE = "wish_images_viewer?$ARG_WISH_ID={$ARG_WISH_ID}&$ARG_WISH_IMAGE_ID={$ARG_WISH_IMAGE_ID}&$ARG_WISH_IMAGE_INDEX={$ARG_WISH_IMAGE_INDEX}"

    fun build(
        wishId: String,
        wishImageId: String,
        wishImageIndex: Int
    ): String {
        return "wish_images_viewer?$ARG_WISH_ID=$wishId&$ARG_WISH_IMAGE_ID=$wishImageId&$ARG_WISH_IMAGE_INDEX=$wishImageIndex"
    }
}