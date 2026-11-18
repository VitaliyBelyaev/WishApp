package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
 * Сетки разделов вне композиции, во ViewModel списка. Когда открыта хотелка, список уходит
 * из композиции, и сетка, созданная заново, знала только позицию, но не то, в какой колонке какая
 * карточка. Раскладка шла от карточки на экране, а при прокрутке вверх карточки выше переезжали
 * между колонками. Живая сетка помнит колонки. После смены сортировки у раздела новая сетка.
 */
internal class SectionGridStates {
    private val states = HashMap<String, Pair<String, LazyStaggeredGridState>>()

    fun getOrCreate(section: String, sort: String, create: () -> LazyStaggeredGridState): LazyStaggeredGridState {
        val kept = states[section]?.takeIf { it.first == sort }?.second
        return kept ?: create().also { states[section] = sort to it }
    }
}

/**
 * Сетка раздела: новая при смене раздела или сортировки, но на запомненной позиции. Пока открыт
 * поиск с запросом, позиция не запоминается: отфильтрованный список короче, а после закрытия
 * поиска раздел всё равно открывается с начала. Сама сетка берётся из [states]; позиция
 * в сохранённом состоянии нужна, только когда процесс пересоздан.
 */
@Composable
internal fun rememberSectionGridState(
    states: SectionGridStates,
    section: ListSection,
    sort: SectionSort,
    isFiltered: Boolean,
): LazyStaggeredGridState {
    val memory = rememberSaveable(saver = SectionScrollMemorySaver) { SectionScrollMemory() }
    val sectionKey = section.toString()
    val sortKey = sort.toString()
    val gridState = remember(states, sectionKey, sortKey) {
        states.getOrCreate(sectionKey, sortKey) {
            val saved = memory.positions[sectionKey]?.takeIf { it.sort == sortKey }
            LazyStaggeredGridState(saved?.index ?: 0, saved?.offset ?: 0)
        }
    }
    LaunchedEffect(gridState, isFiltered) {
        if (isFiltered) return@LaunchedEffect
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (index, offset) -> memory.positions[sectionKey] = SavedScroll(sortKey, index, offset) }
    }
    return gridState
}

private const val SAVED_FIELDS = 4
