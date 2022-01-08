package ru.vitaliy.belyaev.wishapp.ui.screens.wishtags

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.model.repository.analytics.AnalyticsNames
import ru.vitaliy.belyaev.wishapp.model.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.model.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.model.repository.tags.TagsRepository
import ru.vitaliy.belyaev.wishapp.model.repository.wishtagrelation.WishTagRelationRepository
import ru.vitaliy.belyaev.wishapp.navigation.ARG_WISH_ID
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.entity.TagItem
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.entity.toTagItem

@HiltViewModel
class WishTagsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tagsRepository: TagsRepository,
    private val wishTagRelationRepository: WishTagRelationRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val wishId: String = savedStateHandle[ARG_WISH_ID] ?: ""

    private var currentQuery: String = ""
    private var originTagItems: List<TagItem> = emptyList()
    private val _uiState: MutableStateFlow<List<TagItem>> = MutableStateFlow(emptyList())
    val uiState: StateFlow<List<TagItem>> = _uiState
    private val recentlyAddedTagIds: MutableList<String> = mutableListOf()

    init {
        analyticsRepository.trackEvent(AnalyticsNames.Event.SCREEN_VIEW) {
            param(AnalyticsNames.Param.SCREEN_NAME, "WishTags")
        }

        viewModelScope.launch {
            tagsRepository
                .observeAllTags()
                .combine(tagsRepository.observeTagsByWishId(wishId)) { allTags, wishTags ->
                    allTags.map { tag -> tag.toTagItem(wishTags.contains(tag)) }
                }
                .collect { tagItems ->
                    originTagItems = tagItems
                    _uiState.value = prepareTagItems()
                }
        }
    }

    fun onAddTagClicked(tagName: String) {
        analyticsRepository.trackEvent(AnalyticsNames.Event.ADD_NEW_TAG)
        viewModelScope.launch {
            val tagId = UUID.randomUUID().toString()
            recentlyAddedTagIds.add(0, tagId)
            tagsRepository.insertTag(Tag(tagId, tagName.trim()))
            wishTagRelationRepository.insertWishTagRelation(wishId, tagId)
            dataStoreRepository.incrementPositiveActionsCount()
        }
    }

    fun onQueryChanged(query: String) {
        currentQuery = query
        _uiState.value = prepareTagItems()
    }

    fun onTagCheckboxClicked(tagItem: TagItem) {
        viewModelScope.launch {
            val newIsSelected = !tagItem.isSelected
            if (newIsSelected) {
                wishTagRelationRepository.insertWishTagRelation(wishId, tagItem.tag.tagId)
            } else {
                wishTagRelationRepository.deleteWishTagRelation(wishId, tagItem.tag.tagId)
            }
        }
    }

    private fun prepareTagItems(): List<TagItem> {
        val filteredByQuery = originTagItems.filter {
            if (currentQuery.isBlank()) {
                true
            } else {
                it.tag.title.contains(currentQuery, ignoreCase = true)
            }
        }

        val withoutRecentlyAdded = filteredByQuery.filter { it.tag.tagId !in recentlyAddedTagIds }
        val result = mutableListOf<TagItem>().apply {
            for (tagId in recentlyAddedTagIds) {
                val tagItem = filteredByQuery.find { it.tag.tagId == tagId }
                tagItem?.let { add(tagItem) }
            }
            addAll(withoutRecentlyAdded)
        }
        return result
    }
}