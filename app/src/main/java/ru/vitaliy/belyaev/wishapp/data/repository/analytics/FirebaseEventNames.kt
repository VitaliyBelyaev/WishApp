package ru.vitaliy.belyaev.wishapp.data.repository.analytics

/**
 * Приводит имя события к правилам Firebase Analytics: только латиница, цифры и `_`,
 * начинается с буквы, не длиннее 40 символов. Имена с пробелами Firebase молча отбрасывает.
 *
 * Только для Firebase: в Amplitude имена уходят как есть, чтобы не порвать историю.
 */
object FirebaseEventNames {

    const val MAX_LENGTH = 40

    // Длинные префиксы экранов, иначе часть имён упирается в 40 символов
    private val prefixReplacements = listOf(
        "wish_images_viewer_screen_" to "images_",
        "wish_detailed_screen_" to "wish_",
        "wish_list_screen_" to "list_",
        "wish_tags_screen_" to "wish_tags_",
        "edit_tags_screen_" to "edit_tags_",
        "settings_screen_" to "settings_",
        "backup_screen_" to "backup_",
        "about_app_screen_" to "about_",
        "privacy_policy_screen_" to "privacy_",
    )

    private val notAllowedChars = Regex("[^a-z0-9]+")

    fun toFirebaseName(name: String): String {
        var result = name
            .lowercase()
            .replace(notAllowedChars, "_")
            .trim('_')

        prefixReplacements
            .firstOrNull { (prefix, _) -> result.startsWith(prefix) }
            ?.let { (prefix, replacement) -> result = replacement + result.removePrefix(prefix) }

        if (result.firstOrNull()?.isLetter() != true) {
            result = "e_$result"
        }
        return result.take(MAX_LENGTH).trimEnd('_')
    }
}
