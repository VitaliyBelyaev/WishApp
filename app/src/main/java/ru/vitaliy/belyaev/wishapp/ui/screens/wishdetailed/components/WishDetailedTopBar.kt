package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.dropdown.WishappDropDownDefaults
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishDetailedTopBar(
    onBackPressed: () -> Unit,
    onDeleteClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {

    val expanded = remember { mutableStateOf(false) }
    WishAppTopBar(
        "",
        withBackIcon = true,
        onBackPressed = onBackPressed,
        actions = {
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
                        modifier = Modifier.sizeIn(
                            minWidth = WishappDropDownDefaults.dropDownMenuItemMinWidth()
                        ),
                        text = { Text(stringResource(R.string.delete)) },
                        leadingIcon = {
                            ThemedIcon(
                                painterResource(R.drawable.ic_delete),
                                contentDescription = "Delete wish"
                            )
                        },
                        onClick = {
                            expanded.value = false
                            onDeleteClicked()
                        }
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}