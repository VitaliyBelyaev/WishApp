package ru.vitaliy.belyaev.wishapp.ui.screens.edittags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.model.repository.analytics.AnalyticsNames
import ru.vitaliy.belyaev.wishapp.model.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.model.repository.tags.TagsRepository
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItem

@HiltViewModel
class EditTagsViewModel @Inject constructor(
    private val tagsRepository: TagsRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<List<EditTagItem>> = MutableStateFlow(emptyList())
    val uiState: StateFlow<List<EditTagItem>> = _uiState

    private var currentEditingTag: Tag? = null

    init {
        analyticsRepository.trackEvent(AnalyticsNames.Event.SCREEN_VIEW) {
            param(AnalyticsNames.Param.SCREEN_NAME, "EditTags")
        }
        viewModelScope.launch {
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
        viewModelScope.launch {
            tagsRepository.deleteTagsByIds(listOf(tag.tagId))
        }
    }

    fun onEditTagDoneClicked(newTitle: String, tag: Tag) {
        analyticsRepository.trackEvent(AnalyticsNames.Event.EDIT_TAG_DONE_CLICKED_FROM_EDIT_TAGS)
        if (newTitle.isBlank()) {
            return
        }
        viewModelScope.launch {
            currentEditingTag = null
            tagsRepository.updateTagTitle(newTitle, tag.tagId)
        }
    }
}