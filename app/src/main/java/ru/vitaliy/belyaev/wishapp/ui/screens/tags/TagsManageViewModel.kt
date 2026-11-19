package ru.vitaliy.belyaev.wishapp.ui.screens.tags

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Collator
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.EditTagsScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.EditTagsDeleteTagConfirmedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.EditTagsRenameTagDoneClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.EditTagsTagColorChangedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.EditTagsTagCreatedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.UndoAction
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.UndoClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagColor
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagWithWishCount
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.TagsRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel

/**
 * @param tags ярлыки по названию, без удалённых с «Вернуть».
 * @param nextColor цвет, который получит новый ярлык: его показывает строка «Создать».
 */
data class TagsManageState(val tags: List<TagWithWishCount>, val nextColor: TagColor)

sealed interface TagsManageEvent {
    /** Название занято другим ярлыком: `shared` дубли не пропускает, название осталось прежним. */
    data object DuplicateTitle : TagsManageEvent

    /** Снэкбар с «Вернуть»: [onConfirm] удаляет ярлык из базы, когда снэкбар закрылся сам. */
    class Deleted(val title: String, val onUndo: () -> Unit, val onConfirm: () -> Unit) : TagsManageEvent
}

/** Управление ярлыками нового вида: переименование, цвет, создание и удаление с «Вернуть». */
@HiltViewModel
class TagsManageViewModel @Inject constructor(
    private val tagsRepository: TagsRepository,
    private val tagDeleteQueue: TagDeleteQueue,
    private val analyticsRepository: AnalyticsRepository,
) : BaseViewModel() {

    /** `null`, пока ярлыки не прочитаны. */
    val state: StateFlow<TagsManageState?> = combine(
        tagsRepository.observeAllTagsWithWishesCount(),
        tagDeleteQueue.hiddenIds,
    ) { tags, hiddenIds ->
        val collator = Collator.getInstance()
        // Цвет считаем по всем ярлыкам, как его выберет `insertTag`: спрятанные ещё в базе
        val colorCounts = tags.groupingBy { it.tag.color.storedValue }.eachCount().mapValues { it.value.toLong() }
        TagsManageState(
            tags = tags.filter { it.tag.id !in hiddenIds }.sortedWith(compareBy(collator) { it.tag.title }),
            nextColor = TagColor.leastUsed(colorCounts),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    private val _events = Channel<TagsManageEvent>(Channel.BUFFERED)
    val events: Flow<TagsManageEvent> = _events.receiveAsFlow()

    fun trackScreenShow() {
        analyticsRepository.trackEvent(EditTagsScreenShowEvent)
    }

    fun onRename(tagId: String, title: String) {
        val newTitle = title.trim()
        val current = state.value?.tags?.firstOrNull { it.tag.id == tagId }?.tag ?: return
        if (newTitle.isEmpty() || newTitle == current.title) return
        analyticsRepository.trackEvent(EditTagsRenameTagDoneClickedEvent)
        launchSafe {
            if (!tagsRepository.updateTagTitle(newTitle, tagId)) _events.send(TagsManageEvent.DuplicateTitle)
        }
    }

    fun onColor(tagId: String, color: TagColor) {
        val current = state.value?.tags?.firstOrNull { it.tag.id == tagId }?.tag ?: return
        if (current.color == color) return
        analyticsRepository.trackEvent(EditTagsTagColorChangedEvent(tone = color.storedValue))
        launchSafe { tagsRepository.updateTagColor(color, tagId) }
    }

    /**
     * Ярлык с таким названием уже может быть: тогда `shared` вернёт его и новый не создастся.
     * Если это ярлык, удалённый с «Вернуть», он возвращается, иначе снэкбар удалил бы его из базы.
     */
    fun onCreate(title: String) {
        val newTitle = title.trim()
        if (newTitle.isEmpty()) return
        analyticsRepository.trackEvent(EditTagsTagCreatedEvent)
        launchSafe { tagDeleteQueue.undo(tagsRepository.insertTag(newTitle)) }
    }

    fun onDelete(tag: TagEntity) {
        analyticsRepository.trackEvent(EditTagsDeleteTagConfirmedEvent)
        tagDeleteQueue.hide(tag.id)
        launchSafe {
            _events.send(
                TagsManageEvent.Deleted(
                    title = tag.title,
                    onUndo = {
                        analyticsRepository.trackEvent(UndoClickedEvent(UndoAction.DELETE_TAG))
                        tagDeleteQueue.undo(tag.id)
                    },
                    onConfirm = { tagDeleteQueue.commit(tag.id) },
                ),
            )
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
