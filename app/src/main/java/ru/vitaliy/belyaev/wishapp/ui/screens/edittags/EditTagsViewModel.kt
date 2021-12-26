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

@HiltViewModel
class EditTagsViewModel @Inject constructor(
    private val tagsRepository: TagsRepository
) : ViewModel() {

//    private val _uiState: MutableStateFlow<List<EditTagItem>> = MutableStateFlow(emptyList())
//    val uiState: StateFlow<List<EditTagItem>> = _uiState

    private val _uiState: MutableStateFlow<List<Tag>> = MutableStateFlow(emptyList())
    val uiState: StateFlow<List<Tag>> = _uiState

//    private val recentlyAddedTagIds: MutableList<String> = mutableListOf()
//    private var currentlyEditingIdFlow: MutableStateFlow<String> = MutableStateFlow("")

    init {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "EditTags")
        }
//        viewModelScope.launch {
//            tagsRepository
//                .observeAllTags()
//                .combine(currentlyEditingIdFlow) { tags, currentlyEditingId ->
//                    val editTagItems = mutableListOf<EditTagItem>().apply {
//                        val createTagItemMode = if (currentlyEditingId == "") {
//                            EditTagItemMode.EDIT
//                        } else {
//                            EditTagItemMode.DEFAULT
//                        }
//                        add(EditTagItem(null, createTagItemMode))
//                        val tagItems = tags.map {
//                            val tagItemMode = if (currentlyEditingId == it.tagId) {
//                                EditTagItemMode.EDIT
//                            } else {
//                                EditTagItemMode.DEFAULT
//                            }
//                            EditTagItem(it, tagItemMode)
//                        }
//                        addAll(tagItems)
//                    }
//                    editTagItems.toList()
//                }
//                .collect {
//                    _uiState.value = it
//                }
//        }

        viewModelScope.launch {
            tagsRepository
                .observeAllTags()
                .collect { _uiState.value = it }
        }
    }

//    fun onEditTagItemClicked(editTagItem: EditTagItem) {
//        currentlyEditingIdFlow.value = editTagItem.tag?.tagId ?: ""
//    }
//
//    fun onAddTagClicked(tagName: String) {
//        currentlyEditingIdFlow.value = ""
//        viewModelScope.launch {
//            val tagId = UUID.randomUUID().toString()
//            recentlyAddedTagIds.add(0, tagId)
//            tagsRepository.insertTag(Tag(tagId, tagName))
//        }
//    }
//
//    fun onEditDone(tag: Tag) {
//        currentlyEditingIdFlow.value = ""
//        viewModelScope.launch {
//            tagsRepository.updateTagTitle(title = tag.title, tagId = tag.tagId)
//        }
//    }

    fun onTagRemoveClicked(tag: Tag) {
        Firebase.analytics.logEvent("delete_tag_from_edit_tags", null)
        viewModelScope.launch {
            tagsRepository.deleteTagsByIds(listOf(tag.tagId))
        }
    }
}