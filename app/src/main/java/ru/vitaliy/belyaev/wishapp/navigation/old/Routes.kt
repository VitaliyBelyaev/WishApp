package ru.vitaliy.belyaev.wishapp.navigation.old

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ru.vitaliy.belyaev.wishapp.shared.domain.SharedWishDraft
import ru.vitaliy.belyaev.wishapp.ui.IncomingShare

@Serializable
data object MainRoute : NavKey

@Serializable
data class WishDetailedRoute(
    val wishId: String? = null,
    val tagId: String? = null,
    /** Текст, которым поделились из другого приложения, уже разобранный в `shared`. */
    val sharedTitle: String? = null,
    val sharedComment: String? = null,
    val sharedLinks: List<String> = emptyList(),
    /** Картинки оттуда же, `content://`. Читаются один раз, когда создаётся хотелка. */
    val sharedImageUris: List<String> = emptyList(),
) : NavKey {

    fun sharedDraftOrNull(): SharedWishDraft? {
        val draft = SharedWishDraft(
            title = sharedTitle.orEmpty(),
            comment = sharedComment.orEmpty(),
            links = sharedLinks,
        )
        return draft.takeUnless { it.isEmpty }
    }

    companion object {
        fun fromShare(share: IncomingShare): WishDetailedRoute {
            return WishDetailedRoute(
                sharedTitle = share.text?.title,
                sharedComment = share.text?.comment,
                sharedLinks = share.text?.links.orEmpty(),
                sharedImageUris = share.imageUris.map { it.toString() },
            )
        }
    }
}

@Serializable
data object SettingsRoute : NavKey

@Serializable
data object BackupAndRestoreRoute : NavKey

@Serializable
data object AboutAppRoute : NavKey

@Serializable
data object PrivacyPolicyRoute : NavKey

@Serializable
data object EditTagRoute : NavKey

@Serializable
data class WishTagsRoute(
    val wishId: String,
) : NavKey

@Serializable
data class WishImagesViewerRoute(
    val wishId: String,
    val wishImageIndex: Int
) : NavKey
