package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.toValueOfNull
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.linkpreview.LinkPreview
import ru.vitaliy.belyaev.wishapp.ui.core.linkpreview.LinkPreviewLoading
import ru.vitaliy.belyaev.wishapp.ui.core.tags.TagsBlock
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.Data
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.Loading
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.None
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishItem

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Composable
fun WishDetailedScreen(
    onBackPressed: () -> Unit,
    onWishTagsClicked: (String) -> Unit,
    appViewModel: AppActivityViewModel = hiltViewModel(LocalContext.current as AppActivity),
    viewModel: WishDetailedViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val wishItem: Optional<WishItem> by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val handleBackPressed: () -> Unit = {
        keyboardController?.hide()
        viewModel.onBackPressed()
        onBackPressed()
        appViewModel.onWishScreenExit(viewModel.wishId, viewModel.inputWishId.isBlank())
    }
    val openDialog: MutableState<Optional<WishItem>> = remember { mutableStateOf(Optional.empty()) }
    val lazyListState: LazyListState = rememberLazyListState()

    BackHandler { handleBackPressed() }

    Scaffold(
        topBar = {
            WishAppTopBar(
                "",
                withBackIcon = true,
                onBackPressed = handleBackPressed,
                lazyListState = lazyListState,
                actions = {
                    IconButton(onClick = {
                        val wishId = wishItem.toValueOfNull()?.wish?.id ?: return@IconButton
                        onWishTagsClicked(wishId)
                    }) {
                        ThemedIcon(
                            painterResource(R.drawable.ic_label),
                            contentDescription = "Open tags"
                        )
                    }
                    IconButton(onClick = { openDialog.value = wishItem }) {
                        ThemedIcon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete wish"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        if (!wishItem.isPresent) {
            return@Scaffold
        }
        var title: String by remember { mutableStateOf(wishItem.valueOrEmptyString { it.wish.title }) }
        var link: String by remember { mutableStateOf(wishItem.valueOrEmptyString { it.wish.link }) }
        var comment: String by remember { mutableStateOf(wishItem.valueOrEmptyString { it.wish.comment }) }
        val iconsColor: Color = Color.Gray
        val focusRequester = remember { FocusRequester() }

        LazyColumn(state = lazyListState) {
            item {
                TextField(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = title,
                    textStyle = MaterialTheme.typography.h6,
                    onValueChange = { newValue ->
                        title = newValue
                        viewModel.onWishTitleChanged(newValue)
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.enter_title),
                            style = MaterialTheme.typography.h6,
                        )
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
                DisposableEffect(Unit) {
                    if (title.isBlank()) {
                        focusRequester.requestFocus()
                    }
                    onDispose { }
                }
            }
            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = comment,
                    onValueChange = { newValue ->
                        comment = newValue
                        viewModel.onWishCommentChanged(newValue)
                    },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_notes),
                            contentDescription = "Comment",
                            tint = iconsColor
                        )
                    },
                    placeholder = { Text(text = stringResource(R.string.enter_comment)) },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }
            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = link,
                    onValueChange = { newValue ->
                        link = newValue
                        viewModel.onWishLinkChanged(newValue)
                    },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_link),
                            contentDescription = "Link",
                            tint = iconsColor
                        )
                    },
                    placeholder = { Text(text = stringResource(R.string.enter_link)) },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {
                val wishItemValue = wishItem.toValueOfNull()
                val pd = PaddingValues(start = 12.dp, end = 12.dp)
                when (val linkPreviewState = wishItemValue?.linkPreviewState) {
                    is Data -> LinkPreview(linkPreviewState.linkInfo, wishItemValue.wish.link, pd)
                    is Loading -> LinkPreviewLoading(pd)
                    is None -> {
                        //nothing
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {
                val wishItemValue = wishItem.toValueOfNull()
                val tags = wishItemValue?.wish?.tags ?: emptyList()
                TagsBlock(
                    tags = tags,
                    textSize = 16.sp,
                    onClick = {
                        val wishId = wishItem.toValueOfNull()?.wish?.id ?: return@TagsBlock
                        onWishTagsClicked(wishId)
                    },
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp)
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    val wishToDelete = openDialog.value
    if (wishToDelete.isPresent) {
        AlertDialog(
            shape = RoundedCornerShape(dimensionResource(R.dimen.base_corner_radius)),
            onDismissRequest = { openDialog.value = Optional.empty() },
            title = { Text(stringResource(R.string.delete_wish_title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = Optional.empty()
                        viewModel.onDeleteWishClicked()
                        onBackPressed()
                        appViewModel.onDeleteWishClicked(viewModel.wishId)
                    }
                ) {
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colors.onSurface
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { openDialog.value = Optional.empty() }
                ) {
                    Text(
                        stringResource(R.string.cancel),
                        color = MaterialTheme.colors.onSurface
                    )
                }
            }
        )
    }
}

private fun <T> Optional<T>.valueOrEmptyString(extractor: (T) -> String): String =
    if (isPresent) {
        extractor(get())
    } else {
        ""
    }

//@ExperimentalMaterialApi
//@ExperimentalCoroutinesApi
//@Preview
//@Composable
//fun WishDetailedScreenPreview() {
//    WishDetailedScreen(
//        { }
//    )
//}