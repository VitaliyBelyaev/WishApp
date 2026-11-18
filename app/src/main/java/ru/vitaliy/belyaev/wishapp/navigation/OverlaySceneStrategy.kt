package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * Экраны слоем поверх предыдущего, как диалог: предыдущий экран остаётся отрисованным под ним.
 * Так просмотр фото гаснет при свайпе и сквозь него видна хотелка, как чат в Telegram.
 * Вход и выход такой экран анимирует сам, навигация убирает его сразу.
 */
internal class OverlaySceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val last = entries.lastOrNull()
        val isOverlay = last != null && last.metadata[OVERLAY_KEY] == true && entries.size > 1
        return if (isOverlay) EntryOverlayScene(entry = last, previousEntries = entries.dropLast(1)) else null
    }

    companion object {
        private const val OVERLAY_KEY = "overlay"

        /** Метаданные записи, которую показываем слоем. */
        fun overlay(): Map<String, Any> = mapOf(OVERLAY_KEY to true)
    }
}

private class EntryOverlayScene<T : Any>(
    private val entry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
) : OverlayScene<T> {
    override val key: Any = entry.contentKey
    override val entries: List<NavEntry<T>> = listOf(entry)
    override val overlaidEntries: List<NavEntry<T>> = previousEntries
    override val content: @Composable () -> Unit = { entry.Content() }

    override fun equals(other: Any?): Boolean =
        other is EntryOverlayScene<*> && key == other.key && entry == other.entry &&
            previousEntries == other.previousEntries

    override fun hashCode(): Int = (key.hashCode() * 31 + entry.hashCode()) * 31 + previousEntries.hashCode()
}
