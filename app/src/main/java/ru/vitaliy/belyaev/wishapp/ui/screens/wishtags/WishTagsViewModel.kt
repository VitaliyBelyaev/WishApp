package ru.vitaliy.belyaev.wishapp.ui.screens.wishtags

import androidx.lifecycle.SavedStateHandle
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
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.WishesRepository
import ru.vitaliy.belyaev.wishapp.model.repository.wishtagrelation.WishTagRelationRepository
import ru.vitaliy.belyaev.wishapp.navigation.ARG_WISH_ID
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.entity.TagItem
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.entity.toTagItem
import timber.log.Timber

@HiltViewModel
class WishTagsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wishesRepository: WishesRepository,
    private val tagsRepository: TagsRepository,
    private val wishTagRelationRepository: WishTagRelationRepository,
) : ViewModel() {

    private val wishId: String = savedStateHandle[ARG_WISH_ID] ?: ""

    private var currentQuery: String = ""
    private var originTagItems: List<TagItem> = emptyList()
    private val _uiState: MutableStateFlow<List<TagItem>> = MutableStateFlow(emptyList())
    val uiState: StateFlow<List<TagItem>> = _uiState

    init {
        viewModelScope.launch {
            tagsRepository
                .observeAllTags()
                .combine(tagsRepository.observeTagsByWishId(wishId)) { allTags, wishTags ->
                    allTags.map { tag -> tag.toTagItem(wishTags.contains(tag)) }
                }
                .collect { tagItems ->
                    originTagItems = tagItems
                    _uiState.value = filterTagItems()
                    Timber.tag("RTRT").d("filtered:${_uiState.value}")
                }
        }
    }

    fun onAddTagClicked(tagName: String) {
        viewModelScope.launch {
            val tagId = UUID.randomUUID().toString()
            tagsRepository.insertTag(Tag(tagId, tagName))
            wishTagRelationRepository.insertWishTagRelation(wishId, tagId)
        }
    }

    fun onQueryChanged(query: String) {
        currentQuery = query
        _uiState.value = filterTagItems()
        Timber.tag("RTRT").d("onQueryChanged query:$query, list:${_uiState.value}")
    }

    private fun filterTagItems(): List<TagItem> {
        return originTagItems.filter {
            if (currentQuery.isBlank()) {
                true
            } else {
                it.tag.title.contains(currentQuery, ignoreCase = true)
            }
        }
    }
}