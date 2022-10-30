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

    private val testListFlow: MutableSharedFlow<List<TestItem>> = MutableSharedFlow()

    private val _uiState: MutableStateFlow<TestScreenState> =
        MutableStateFlow(TestScreenState(items = list.sortedBy { it.position }))
    val uiState: StateFlow<TestScreenState> = _uiState.asStateFlow()

    init {
//        launchSafe {
//            testListFlow
//                .collect {
//                    _uiState.value = uiState.value.copy(items = it.sortedBy { it.position })
//                }
//        }
//
//        launchSafe {
//            testListFlow.emit(list)
//        }
    }

    fun onMove(from: ItemPosition, to: ItemPosition) {
        val currentList = uiState.value.items
        val fromItem = currentList.find { it.id == from.key }
        val toItem = currentList.find { it.id == to.key }
        val newList = currentList.map {
            when (it.id) {
                to.key -> {
                    it.copy(position = fromItem!!.position)
                }
                from.key -> {
                    it.copy(position = toItem!!.position)
                }
                else -> {
                    it
                }
            }
        }
        _uiState.value = uiState.value.copy(items = newList.sortedBy { it.position })
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