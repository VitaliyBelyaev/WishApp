package ru.vitaliy.belyaev.wishapp.ui.screens.edittags

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vitaliy.belyaev.wishapp.data.database.Tag
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsNames
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.tags.TagsRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItem

@HiltViewModel
class EditTagsViewModel @Inject constructor(
    private val tagsRepository: TagsRepository,
    private val analyticsRepository: AnalyticsRepository
) : BaseViewModel() {

    private val _uiState: MutableStateFlow<List<EditTagItem>> = MutableStateFlow(emptyList())
    val uiState: StateFlow<List<EditTagItem>> = _uiState

    private var currentEditingTag: Tag? = null

    init {
        analyticsRepository.trackEvent(AnalyticsNames.Event.SCREEN_VIEW) {
            param(AnalyticsNames.Param.SCREEN_NAME, "EditTags")
        }
        launchSafe {
            tagsRepository
                .observeAllTags()
                .collect { tags ->
                    _uiState.value = tags.map { EditTagItem(it, it == currentEditingTag) }
                }
        }
    }

    fun onTagClicked(tag: Tag) {
        analyticsRepository.trackEvent(AnalyticsNames.Event.TAG_CLICKED_FROM_EDIT_TAGS)
        currentEditingTag = tag
        _uiState.value = _uiState.value.map { it.copy(isEditMode = it.tag == currentEditingTag) }
    }

    fun onTagRemoveClicked(tag: Tag) {
        analyticsRepository.trackEvent(AnalyticsNames.Event.DELETE_TAG_FROM_EDIT_TAGS)
        launchSafe {
            tagsRepository.deleteTagsByIds(listOf(tag.tagId))
        }
    }

    fun onEditTagDoneClicked(newTitle: String, tag: Tag) {
        analyticsRepository.trackEvent(AnalyticsNames.Event.EDIT_TAG_DONE_CLICKED_FROM_EDIT_TAGS)
        if (newTitle.isBlank()) {
            return
        }
        launchSafe {
            currentEditingTag = null
            tagsRepository.updateTagTitle(newTitle.trim(), tag.tagId)
        }
    }
}