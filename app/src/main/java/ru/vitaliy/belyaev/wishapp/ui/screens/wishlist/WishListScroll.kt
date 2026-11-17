package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow

/**
 * Прокрутка каждого раздела: хотелки, «Сбылось» и каждый ярлык открываются там, где их оставили,
 * как обычные вкладки. Сетка у раздела всё равно своя, новая: одна сетка на всё рисовала первый
 * кадр нового раздела на старой прокрутке. Позиция запоминается вместе с сортировкой, и после
 * смены сортировки раздел открывается с начала.
 */
private class SectionScrollMemory(
    val positions: MutableMap<String, SavedScroll> = mutableMapOf(),
)

private data class SavedScroll(val sort: String, val index: Int, val offset: Int)

private val SectionScrollMemorySaver = listSaver<SectionScrollMemory, Any>(
    save = { memory ->
        memory.positions.flatMap { (section, scroll) -> listOf(section, scroll.sort, scroll.index, scroll.offset) }
    },
    restore = { saved ->
        SectionScrollMemory(
            saved.chunked(SAVED_FIELDS).associateTo(mutableMapOf()) { fields ->
                fields[0] as String to SavedScroll(
                    sort = fields[1] as String,
                    index = fields[2] as Int,
                    offset = fields[3] as Int,
                )
            },
        )
    },
)

/**
 * Сетка раздела: новая при смене раздела или сортировки, но на запомненной позиции. Пока открыт
 * поиск с запросом, позиция не запоминается: отфильтрованный список короче, а после закрытия
 * поиска раздел всё равно открывается с начала.
 */
@Composable
internal fun rememberSectionGridState(
    section: ListSection,
    sort: SectionSort,
    isFiltered: Boolean,
): LazyStaggeredGridState {
    val memory = rememberSaveable(saver = SectionScrollMemorySaver) { SectionScrollMemory() }
    val sectionKey = section.toString()
    val sortKey = sort.toString()
    val gridState = rememberSaveable(sectionKey, sortKey, saver = LazyStaggeredGridState.Saver) {
        val saved = memory.positions[sectionKey]?.takeIf { it.sort == sortKey }
        LazyStaggeredGridState(saved?.index ?: 0, saved?.offset ?: 0)
    }
    LaunchedEffect(gridState, isFiltered) {
        if (isFiltered) return@LaunchedEffect
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (index, offset) -> memory.positions[sectionKey] = SavedScroll(sortKey, index, offset) }
    }
    return gridState
}

private const val SAVED_FIELDS = 4
