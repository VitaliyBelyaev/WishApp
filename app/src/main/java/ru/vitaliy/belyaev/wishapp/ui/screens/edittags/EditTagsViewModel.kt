package ru.vitaliy.belyaev.wishapp.ui.screens.edittags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.model.repository.tags.TagsRepository
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItem
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItemMode

@HiltViewModel
class EditTagsViewModel @Inject constructor(
    private val tagsRepository: TagsRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<List<EditTagItem>> = MutableStateFlow(emptyList())
    val uiState: StateFlow<List<EditTagItem>> = _uiState

    private var originEditTagItems: List<EditTagItem> = emptyList()
    private val recentlyAddedTagIds: MutableList<String> = mutableListOf()
    private var currentlyEditingIdFlow: MutableStateFlow<String> = MutableStateFlow("")

    init {
        viewModelScope.launch {
            tagsRepository
                .observeAllTags()
                .combine(currentlyEditingIdFlow) { tags, currentlyEditingId ->
                    val editTagItems = mutableListOf<EditTagItem>().apply {
                        val createTagItemMode = if (currentlyEditingId == "") {
                            EditTagItemMode.EDIT
                        } else {
                            EditTagItemMode.DEFAULT
                        }
                        add(EditTagItem(null, createTagItemMode))
                        val tagItems = tags.map {
                            val tagItemMode = if (currentlyEditingId == it.tagId) {
                                EditTagItemMode.EDIT
                            } else {
                                EditTagItemMode.DEFAULT
                            }
                            EditTagItem(it, tagItemMode)
                        }
                        addAll(tagItems)
                    }
                    editTagItems.toList()
                }
                .collect {
                    _uiState.value = it
                }
        }
    }

    fun onEditTagItemClicked(editTagItem: EditTagItem) {
        currentlyEditingIdFlow.value = editTagItem.tag?.tagId ?: ""
    }

    fun onAddTagClicked(tagName: String) {
        currentlyEditingIdFlow.value = ""
        viewModelScope.launch {
            val tagId = UUID.randomUUID().toString()
            recentlyAddedTagIds.add(0, tagId)
            tagsRepository.insertTag(Tag(tagId, tagName))
        }
    }

    fun onEditDone(tag: Tag) {
        currentlyEditingIdFlow.value = ""
        viewModelScope.launch {
            tagsRepository.updateTagTitle(title = tag.title, tagId = tag.tagId)
        }
    }

    fun onTagRemoveClicked(tag: Tag) {
        currentlyEditingIdFlow.value = ""
        viewModelScope.launch {
            tagsRepository.deleteTagsByIds(listOf(tag.tagId))
        }
    }
}