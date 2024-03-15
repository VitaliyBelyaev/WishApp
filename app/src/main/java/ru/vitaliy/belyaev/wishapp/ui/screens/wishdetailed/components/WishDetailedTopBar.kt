package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.vitaliy.belyaev.wishapp.ui.core.dropdown.DeleteDropDownItem
import ru.vitaliy.belyaev.wishapp.ui.core.dropdown.MenuMoreWithDropDown
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.WishItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishDetailedTopBar(
    wishItem: WishItem?,
    onBackPressed: () -> Unit,
    onDeleteClicked: () -> Unit,
    onWishCompletedClicked: (wishId: String, oldIsCompleted: Boolean) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {

    WishAppTopBar(
        "",
        withBackIcon = true,
        onBackPressed = onBackPressed,
        actions = {
            MenuMoreWithDropDown { expanded ->
                wishItem?.let {
                    MoveToCompletedDropDownItem(
                        wishItem = it,
                        onWishCompletedClicked = { wishId, oldIsCompleted ->
                            expanded.value = false
                            onWishCompletedClicked(wishId, oldIsCompleted)
                        }
                    )
                }

                DeleteDropDownItem {
                    expanded.value = false
                    onDeleteClicked()
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun WishDetailedTopBarPreview() {

    WishDetailedTopBar(
        null,
        onBackPressed = {},
        onDeleteClicked = {},
        onWishCompletedClicked = { _, _ -> }
    )
}