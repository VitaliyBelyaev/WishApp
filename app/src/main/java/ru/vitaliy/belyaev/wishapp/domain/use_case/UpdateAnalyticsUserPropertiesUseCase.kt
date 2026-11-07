package ru.vitaliy.belyaev.wishapp.domain.use_case

import java.util.Locale
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.TagsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository

/**
 * Снимок свойств пользователя для аналитики. Отправляется при запуске и при смене
 * темы или флага бэкапа: так свойства есть у каждой сессии, а БД не читается на каждое изменение.
 */
class UpdateAnalyticsUserPropertiesUseCase @Inject constructor(
    private val wishesRepository: WishesRepository,
    private val tagsRepository: TagsRepository,
    private val analyticsRepository: AnalyticsRepository,
) {

    suspend operator fun invoke(theme: Theme, isBackupCreated: Boolean) {
        val currentWishes = wishesRepository.getAllWishes(isCompleted = false)
        val completedWishes = wishesRepository.getAllWishes(isCompleted = true)
        val hasImages = currentWishes.any { it.images.isNotEmpty() } ||
            completedWishes.any { it.images.isNotEmpty() }

        analyticsRepository.setUserProperties(
            mapOf(
                // Вид интерфейса: до редизайна у всех old
                "ui_look" to "old",
                "wishes_count" to currentWishes.size,
                "completed_wishes_count" to completedWishes.size,
                "tags_count" to tagsRepository.getAllTags().size,
                "has_images" to hasImages,
                "app_theme" to theme.name.lowercase(Locale.ROOT),
                "backup_created" to isBackupCreated,
            ),
        )
    }
}
