package ru.vitaliy.belyaev.wishapp.ui.screens.edittags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.model.repository.tags.TagsRepository
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItem

@HiltViewModel
class EditTagsViewModel @Inject constructor(
    private val tagsRepository: TagsRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<List<EditTagItem>> = MutableStateFlow(emptyList())
    val uiState: StateFlow<List<EditTagItem>> = _uiState

    private var currentEditingTag: Tag? = null

    init {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "EditTags")
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
        Firebase.analytics.logEvent("tag_clicked_from_edit_tags", null)
        currentEditingTag = tag
        _uiState.value = _uiState.value.map { it.copy(inEditMode = it.tag == currentEditingTag) }
    }

    fun onTagRemoveClicked(tag: Tag) {
        Firebase.analytics.logEvent("delete_tag_from_edit_tags", null)
        viewModelScope.launch {
            tagsRepository.deleteTagsByIds(listOf(tag.tagId))
        }
    }

    fun onEditTagDoneClicked(newTitle: String, tag: Tag) {
        Firebase.analytics.logEvent("edit_tag_done_clicked_from_edit_tags", null)
        viewModelScope.launch {
            currentEditingTag = null
            tagsRepository.updateTagTitle(newTitle, tag.tagId)
        }
    }
}