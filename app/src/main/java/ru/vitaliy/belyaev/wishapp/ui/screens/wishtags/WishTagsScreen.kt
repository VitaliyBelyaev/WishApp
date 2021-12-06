package ru.vitaliy.belyaev.wishapp.ui.screens.wishtags

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.components.AddTagBlock

@Composable
fun WishTagsScreen(
    onBackPressed: () -> Unit,
    viewModel: WishTagsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var query: String by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {

                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = query,
                        textStyle = MaterialTheme.typography.body1,
                        onValueChange = { newValue ->
                            query = newValue
                            viewModel.onQueryChanged(newValue)
                        },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.enter_label_name),
                                style = MaterialTheme.typography.body1,
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = colorResource(R.color.inputCursorColor)
                        ),
                        trailingIcon = {
                            if (query.isNotBlank()) {
                                val shape = RoundedCornerShape(50.dp)
                                Icon(
                                    Icons.Filled.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier
                                        .clip(shape)
                                        .clickable { query = "" }
                                )
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed.invoke() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = colorResource(R.color.toolbarColor)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {

        LazyColumn {
            if (query.isNotBlank()) {
                item {
                    AddTagBlock(
                        tagName = query,
                        onClick = {}
                    )
                }
            }
//            items(state.wishes) { wishItem ->
//                val isSelected: Boolean = state.selectedIds.contains(wishItem.wish.id)
//                WishItemBlock(
//                    wishItem = wishItem,
//                    isSelected = isSelected,
//                    onWishClicked = onWishClicked,
//                    onWishLongPress = { wish -> viewModel.onWishLongPress(wish) }
//                )
//            }
        }
    }
}

@Preview
@Composable
fun WishTagsScreenPreview() {
    WishTagsScreen(
        { }
    )
}