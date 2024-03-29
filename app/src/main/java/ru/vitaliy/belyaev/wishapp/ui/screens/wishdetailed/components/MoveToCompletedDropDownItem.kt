package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.dropdown.WishappDropDownDefaults
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.WishItem

@Composable
fun ColumnScope.MoveToCompletedDropDownItem(
    wishItem: WishItem,
    onWishCompletedClicked: (wishId: String, oldIsCompleted: Boolean) -> Unit,
) {
    val isCompleted = wishItem.wish.isCompleted

    val text = if (isCompleted) {
        stringResource(R.string.wish_not_done)
    } else {
        stringResource(R.string.wish_done)
    }

    val iconPainter = if (isCompleted) {
        painterResource(R.drawable.ic_undo)
    } else {
        painterResource(R.drawable.ic_check)
    }

    val contentDescription = if (isCompleted) {
        "Move wish to current"
    } else {
        "Move wish to completed"
    }

    DropdownMenuItem(
        modifier = Modifier.sizeIn(
            minWidth = WishappDropDownDefaults.dropDownMenuItemMinWidth()
        ),
        text = { Text(text) },
        leadingIcon = {
            ThemedIcon(
                iconPainter,
                contentDescription = contentDescription
            )
        },
        onClick = { onWishCompletedClicked(wishItem.wish.id, isCompleted) }
    )
}