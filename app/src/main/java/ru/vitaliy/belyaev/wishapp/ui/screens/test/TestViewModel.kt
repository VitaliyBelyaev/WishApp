package ru.vitaliy.belyaev.wishapp.ui.screens.test

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.burnoutcrew.reorderable.ItemPosition
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel

@HiltViewModel
class TestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel() {

    private var list: List<TestItem> = createTestItems()

    var list2 by mutableStateOf(list)

    private val testListFlow: MutableSharedFlow<List<TestItem>> = MutableSharedFlow()

    private val _uiState: MutableStateFlow<List<TestItem>> = MutableStateFlow(listOf())
    val uiState: StateFlow<List<TestItem>> = _uiState.asStateFlow()

    init {
        launchSafe {
            testListFlow
                .collect {
                    _uiState.value = it.sortedByDescending { it.position }
                }
        }

        launchSafe {
            testListFlow.emit(list)
        }
    }

    fun onMove(from: ItemPosition, to: ItemPosition) {
//        list2 = list2.toMutableList().apply {
//            add(to.index, removeAt(from.index))
//        }

        val fromItem = list.find { it.id == from.key }
        val toItem = list.find { it.id == to.key }
        val newList = list.map {
            when (it.id) {
                from.key -> {
                    it.copy(position = toItem!!.position)
                }
                to.key -> {
                    it.copy(position = fromItem!!.position)
                }
                else -> {
                    it
                }
            }
        }
        list = newList
        launchSafe {
            testListFlow.emit(newList)
        }
    }

    private fun createTestItems(): MutableList<TestItem> {
        val items = mutableListOf<TestItem>()
        for (i in 0..39) {
            items.add(
                TestItem(
                    id = UUID.randomUUID().toString(),
                    position = i,
                    title = "Test item: $i"
                )
            )
        }
        return items
    }
}

data class TestItem(
    val id: String,
    val position: Int,
    val title: String
)