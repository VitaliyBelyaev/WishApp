package ru.vitaliy.belyaev.wishapp.ui.core.dropdown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon

@Composable
fun RowScope.MenuMoreWithDropDown(
    dropDownItems: @Composable ColumnScope.(MutableState<Boolean>) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    Box(Modifier.wrapContentSize(Alignment.TopEnd)) {
        IconButton(onClick = { expanded.value = true }) {
            ThemedIcon(
                Icons.Filled.MoreVert,
                contentDescription = "Menu"
            )
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
        ) {
            dropDownItems(expanded)
        }
    }
}