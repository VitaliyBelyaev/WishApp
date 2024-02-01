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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.ui.core.dropdown.DeleteDropDownItem
import ru.vitaliy.belyaev.wishapp.ui.core.dropdown.MenuMoreWithDropDown
import ru.vitaliy.belyaev.wishapp.ui.core.dropdown.WishappDropDownDefaults
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_images.WishImagesViewerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishDetailedTopBar(
    onBackPressed: () -> Unit,
    onDeleteClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {


    WishAppTopBar(
        "",
        withBackIcon = true,
        onBackPressed = onBackPressed,
        actions = {
            MenuMoreWithDropDown { expanded ->
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
        onBackPressed = {},
        onDeleteClicked = {},
    )
}