package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import androidx.compose.runtime.Immutable
import ru.vitaliy.belyaev.wishapp.domain.model.DoneSortMode
import ru.vitaliy.belyaev.wishapp.domain.model.ListLayout
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishSortMode
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishCardModel

/** Раздел списка: вкладка в панели или выбранный в шторке ярлык. */
sealed interface ListSection {
    data object Wishes : ListSection
    data object Done : ListSection
    data class Tag(val tagId: String) : ListSection
}

/** Сортировка текущего раздела. У «Сбылось» свой набор: «Моего порядка» там нет. */
sealed interface SectionSort {
    val isDefault: Boolean

    data class Main(val mode: WishSortMode) : SectionSort {
        override val isDefault: Boolean get() = mode == WishSortMode.Default
    }

    data class Done(val mode: DoneSortMode) : SectionSort {
        override val isDefault: Boolean get() = mode == DoneSortMode.DEFAULT
    }
}

/** Ярлык в шторке: число текущих хотелок с ним. */
@Immutable
data class TagSheetItem(val tag: TagEntity, val wishesCount: Long)

/**
 * Поиск по разделу. [otherSectionMatches] — сколько хотелок с тем же запросом в соседнем разделе:
 * для «Хотелок» и ярлыка это «Сбылось», для «Сбылось» это «Хотелки».
 */
@Immutable
data class SearchState(
    val query: String,
    val otherSectionMatches: Int,
)

@Immutable
data class WishListUiState(
    val section: ListSection = ListSection.Wishes,
    /** Выбранный ярлык, пока он есть в базе. */
    val selectedTag: TagEntity? = null,
    val cards: List<WishCardModel> = emptyList(),
    val isLoading: Boolean = true,
    val layout: ListLayout = ListLayout.CARDS,
    val sort: SectionSort = SectionSort.Main(WishSortMode.Default),
    val doneCount: Long = 0,
    val tags: List<TagSheetItem> = emptyList(),
    /** `null`, пока поиск закрыт. */
    val search: SearchState? = null,
) {
    /** Сколько хотелок в разделе без учёта поиска: от этого зависят пустое состояние и пункты меню. */
    val sectionIsEmpty: Boolean get() = !isLoading && cards.isEmpty() && search == null
}
