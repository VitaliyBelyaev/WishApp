package ru.vitaliy.belyaev.wishapp.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object MainRoute : NavKey

@Serializable
data class WishDetailedRoute(
    val wishId: String? = null,
    val wishLink: String? = null,
    val tagId: String? = null
) : NavKey

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
