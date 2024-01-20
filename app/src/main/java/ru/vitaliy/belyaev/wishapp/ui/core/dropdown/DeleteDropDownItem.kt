package ru.vitaliy.belyaev.wishapp.ui.core.dropdown

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon

@Composable
fun ColumnScope.DeleteDropDownItem(
    onClicked: () -> Unit
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
        onClick = onClicked
    )
}