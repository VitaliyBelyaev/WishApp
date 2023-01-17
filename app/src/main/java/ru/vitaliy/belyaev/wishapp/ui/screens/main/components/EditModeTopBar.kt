package ru.vitaliy.belyaev.wishapp.ui.screens.main.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditModeTopBar(
    selectedCount: Int,
    onCloseEditModeClicked: () -> Unit,
    onDeleteSelectedClicked: () -> Unit,
    onSelectAllClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val expanded = remember { mutableStateOf(false) }
    TopAppBar(
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
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(R.string.select_all))
                        },
                        onClick = {
                            expanded.value = false
                            onSelectAllClicked()
                        }
                    )
                }
            }

        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun EditModeTopBarPreview() {
    EditModeTopBar(3, { }, { }, { }, null)
}