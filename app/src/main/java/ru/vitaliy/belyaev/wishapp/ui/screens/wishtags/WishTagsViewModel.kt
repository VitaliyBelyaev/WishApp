package ru.vitaliy.belyaev.wishapp.ui.screens.wishtags

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.entity.analytics.WishTagsScreenShowEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.WishTagsAddNewTagClickedEvent
import ru.vitaliy.belyaev.wishapp.navigation.ARG_WISH_ID
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.TagsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishTagRelationRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.entity.TagItem
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.entity.toTagItem

@HiltViewModel
class WishTagsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tagsRepository: TagsRepository,
    private val wishTagRelationRepository: WishTagRelationRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val analyticsRepository: AnalyticsRepository
) : BaseViewModel() {

    private val wishId: String = savedStateHandle[ARG_WISH_ID] ?: ""

    private var currentQuery: String = ""
    private var originTagItems: List<TagItem> = emptyList()
    private val _uiState: MutableStateFlow<List<TagItem>> = MutableStateFlow(emptyList())
    val uiState: StateFlow<List<TagItem>> = _uiState
    private val recentlyAddedTagIds: MutableList<String> = mutableListOf()

    init {
        launchSafe {
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

    fun trackScreenShow() {
        analyticsRepository.trackEvent(WishTagsScreenShowEvent)
    }

    fun onAddTagClicked(tagName: String) {
        analyticsRepository.trackEvent(WishTagsAddNewTagClickedEvent)
        launchSafe {
            val tagId = tagsRepository.insertTag(tagName.trim())
            recentlyAddedTagIds.add(0, tagId)
            wishTagRelationRepository.insertWishTagRelation(wishId, tagId)
            dataStoreRepository.incrementPositiveActionsCount()
        }
    }

    fun onQueryChanged(query: String) {
        currentQuery = query
        _uiState.value = prepareTagItems()
    }

    fun onTagCheckboxClicked(tagItem: TagItem) {
        launchSafe {
            val newIsSelected = !tagItem.isSelected
            if (newIsSelected) {
                wishTagRelationRepository.insertWishTagRelation(wishId, tagItem.tag.id)
            } else {
                wishTagRelationRepository.deleteWishTagRelation(wishId, tagItem.tag.id)
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

        val withoutRecentlyAdded = filteredByQuery.filter { it.tag.id !in recentlyAddedTagIds }
        val result = mutableListOf<TagItem>().apply {
            for (tagId in recentlyAddedTagIds) {
                val tagItem = filteredByQuery.find { it.tag.id == tagId }
                tagItem?.let { add(tagItem) }
            }
            addAll(withoutRecentlyAdded)
        }
        return result
    }
}