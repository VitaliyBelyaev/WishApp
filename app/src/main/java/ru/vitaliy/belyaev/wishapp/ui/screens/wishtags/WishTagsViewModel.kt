package ru.vitaliy.belyaev.wishapp.ui.screens.wishtags

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.domain.WishInteractor
import ru.vitaliy.belyaev.wishapp.model.repository.tags.TagsRepository
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.WishesRepository
import ru.vitaliy.belyaev.wishapp.model.repository.wishtagrelation.WishTagRelationRepository

@HiltViewModel
class WishTagsViewModel @Inject constructor(
    private val wishesRepository: WishesRepository,
    private val tagsRepository: TagsRepository,
    private val wishTagRelationRepository: WishTagRelationRepository,
    private val wishInteractor: WishInteractor
) : ViewModel() {

    fun onQueryChanged(query: String) {
    }
}