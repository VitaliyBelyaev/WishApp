package ru.vitaliy.belyaev.wishapp.ui.screens.edittags

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.entity.analytics.EditTagsScreenShowEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.EditTagsDeleteTagConfirmedEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.EditTagsRenameTagDoneClickedEvent
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.TagsRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItem

@HiltViewModel
class EditTagsViewModel @Inject constructor(
    private val tagsRepository: TagsRepository,
    private val analyticsRepository: AnalyticsRepository
) : BaseViewModel() {

    private val _uiState: MutableStateFlow<List<EditTagItem>> = MutableStateFlow(emptyList())
    val uiState: StateFlow<List<EditTagItem>> = _uiState

    private var currentEditingTag: TagEntity? = null

    init {
        launchSafe {
            tagsRepository
                .observeAllTags()
                .collect { tags ->
                    _uiState.value = tags.map { EditTagItem(it, it == currentEditingTag) }
                }
        }
    }

    fun trackScreenShow() {
        analyticsRepository.trackEvent(EditTagsScreenShowEvent)
    }

    fun onTagClicked(tag: TagEntity) {
        currentEditingTag = tag
        _uiState.value = _uiState.value.map { it.copy(isEditMode = it.tag == currentEditingTag) }
    }

    fun onTagRemoveClicked(tag: TagEntity) {
        analyticsRepository.trackEvent(EditTagsDeleteTagConfirmedEvent)
        launchSafe {
            tagsRepository.deleteTagsByIds(listOf(tag.id))
        }
    }

    fun onEditTagDoneClicked(newTitle: String, tag: TagEntity) {
        analyticsRepository.trackEvent(EditTagsRenameTagDoneClickedEvent)
        if (newTitle.isBlank()) {
            return
        }
        launchSafe {
            currentEditingTag = null
            tagsRepository.updateTagTitle(newTitle.trim(), tag.id)
        }
    }
}