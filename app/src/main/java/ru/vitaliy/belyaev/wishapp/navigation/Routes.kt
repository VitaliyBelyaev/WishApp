package ru.vitaliy.belyaev.wishapp.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

const val ARG_WISH_ID = "wishId"
const val ARG_WISH_LINK = "wishLink"
const val ARG_WISH_IMAGE_ID = "wishImageId"
const val ARG_WISH_IMAGE_INDEX = "wishImageIndex"
const val ARG_TAG_ID = "tagId"

@Serializable
data object MainRoute2 : NavKey

@Serializable
data class WishDetailedRoute2(
    val wishId: String? = null,
    val wishLink: String? = null,
    val tagId: String? = null
) : NavKey

@Serializable
data object SettingsRoute2 : NavKey

@Serializable
data object BackupAndRestoreRoute2 : NavKey

@Serializable
data object AboutAppRoute2 : NavKey

@Serializable
data object PrivacyPolicyRoute2 : NavKey

@Serializable
data object EditTagRoute2 : NavKey

@Serializable
data class WishTagsRoute2(
    val wishId: String,
) : NavKey

@Serializable
data class WishImagesViewerRoute2(
    val wishId: String,
    val wishImageId: String,
    val wishImageIndex: Int
) : NavKey

object MainRoute {
    const val VALUE = "main"
}

object WishDetailedRoute {

    const val VALUE =
        "wish_detailed?$ARG_WISH_ID={$ARG_WISH_ID}&$ARG_WISH_LINK={$ARG_WISH_LINK}&$ARG_TAG_ID={$ARG_TAG_ID}"

    fun buildRoute(
        wishId: String? = null,
        wishLink: String? = null,
        tagId: String? = null
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
            if (!tagId.isNullOrBlank()) {
                append("$separator$ARG_TAG_ID=$tagId")
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
    const val VALUE =
        "wish_images_viewer?$ARG_WISH_ID={$ARG_WISH_ID}&$ARG_WISH_IMAGE_ID={$ARG_WISH_IMAGE_ID}&$ARG_WISH_IMAGE_INDEX={$ARG_WISH_IMAGE_INDEX}"

    fun build(
        wishId: String,
        wishImageId: String,
        wishImageIndex: Int
    ): String {
        return "wish_images_viewer?$ARG_WISH_ID=$wishId&$ARG_WISH_IMAGE_ID=$wishImageId&$ARG_WISH_IMAGE_INDEX=$wishImageIndex"
    }
}