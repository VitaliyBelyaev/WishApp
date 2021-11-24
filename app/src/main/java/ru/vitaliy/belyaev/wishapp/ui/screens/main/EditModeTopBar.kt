package ru.vitaliy.belyaev.wishapp.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.vitaliy.belyaev.wishapp.R

@Composable
fun EditModeTopBar(
    selectedCount: Int,
    onCloseEditModeClicked: () -> Unit,
    onDeleteSelectedClicked: () -> Unit,
    onSelectAllClicked: () -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(text = selectedCount.toString()) },
        navigationIcon = {
            IconButton(onClick = onCloseEditModeClicked) {
                Icon(Icons.Filled.Clear, contentDescription = "Close")
            }
        },
        actions = {
            IconButton(onClick = onDeleteSelectedClicked) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete"
                )
            }
            Box(Modifier.wrapContentSize(Alignment.TopEnd)) {
                IconButton(onClick = {
                    expanded.value = true
                }) {
                    Icon(
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
        backgroundColor = colorResource(R.color.toolbarColor)
    )
}

@Composable
@Preview
fun EditModeTopBarPreview() {
    EditModeTopBar(3, { }, { }, { })
}