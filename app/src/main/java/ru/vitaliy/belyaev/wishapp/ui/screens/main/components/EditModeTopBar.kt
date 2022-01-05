package ru.vitaliy.belyaev.wishapp.ui.screens.main.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.ScrollAwareTopBar

@Composable
fun EditModeTopBar(
    selectedCount: Int,
    onCloseEditModeClicked: () -> Unit,
    onDeleteSelectedClicked: () -> Unit,
    onSelectAllClicked: () -> Unit,
    lazyListState: LazyListState? = null
) {
    val expanded = remember { mutableStateOf(false) }
    ScrollAwareTopBar(
        title = { Text(text = selectedCount.toString()) },
        navigationIcon = {
            IconButton(onClick = onCloseEditModeClicked) {
                ThemedIcon(Icons.Filled.Clear, contentDescription = "Close")
            }
        },
        actions = {
            IconButton(onClick = onDeleteSelectedClicked) {
                ThemedIcon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete"
                )
            }
            Box(Modifier.wrapContentSize(Alignment.TopEnd)) {
                IconButton(onClick = {
                    expanded.value = true
                }) {
                    ThemedIcon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Menu"
                    )
                }

                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                ) {
                    DropdownMenuItem(onClick = {
                        expanded.value = false
                        onSelectAllClicked()
                    }) {
                        Text(stringResource(R.string.select_all))
                    }
                }
            }

        },
        lazyListState = lazyListState
    )
}

@Composable
@Preview
fun EditModeTopBarPreview() {
    EditModeTopBar(3, { }, { }, { })
}