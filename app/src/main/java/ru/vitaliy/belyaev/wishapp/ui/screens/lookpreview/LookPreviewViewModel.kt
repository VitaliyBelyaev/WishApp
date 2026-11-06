package ru.vitaliy.belyaev.wishapp.ui.screens.lookpreview

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishCardModel
import ru.vitaliy.belyaev.wishapp.ui.components.wish.toWishCardModel
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel

private const val ACTIVE_LIMIT = 8
private const val COMPLETED_LIMIT = 2

/** Настоящие хотелки для витрины: так видно, как карточка ведёт себя на реальных данных и фото. */
@HiltViewModel
class LookPreviewViewModel @Inject constructor(
    wishesRepository: WishesRepository,
) : BaseViewModel() {

    private val _cards = MutableStateFlow<List<WishCardModel>>(emptyList())
    val cards: StateFlow<List<WishCardModel>> = _cards

    init {
        launchSafe {
            combine(
                wishesRepository.observeAllWishes(isCompleted = false),
                wishesRepository.observeAllWishes(isCompleted = true),
            ) { active, completed ->
                (active.take(ACTIVE_LIMIT) + completed.take(COMPLETED_LIMIT)).map { it.toWishCardModel() }
            }.collect { _cards.value = it }
        }
    }
}
