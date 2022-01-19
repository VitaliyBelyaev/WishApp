package ru.vitaliy.belyaev.wishapp.ui.screens.edittags

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.components.EditTagBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItem
import timber.log.Timber

@ExperimentalComposeUiApi
@Composable
fun EditTagsScreen(
    onBackPressed: () -> Unit,
    viewModel: EditTagsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val editTagItems: List<EditTagItem> by viewModel.uiState.collectAsState()
    val openDialog: MutableState<Optional<Tag>> = remember { mutableStateOf(Optional.empty()) }
    val lazyListState: LazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current
    val handleBackPressed: () -> Unit = {
        keyboardController?.hide()
        onBackPressed()
    }

    BackHandler { handleBackPressed() }

    Scaffold(
        topBar = {
            WishAppTopBar(
                title = stringResource(R.string.edit_tags),
                withBackIcon = true,
                onBackPressed = handleBackPressed,
                lazyListState = lazyListState
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {

        LazyColumn(state = lazyListState) {
            itemsIndexed(editTagItems) { index, editTagItem ->
                EditTagBlock(
                    editTagItem = editTagItem,
                    editTagItemIndex = index,
                    onClick = { clickedTag -> viewModel.onTagClicked(clickedTag) },
                    onRemoveClick = { openDialog.value = Optional.of(it) },
                    onEditDoneClick = { viewModel.onEditTagDoneClicked(it, editTagItem.tag) },
                    onEditingItemFocusRequested = {
                        coroutineScope.launch {
                            delay(200)
                            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
                            val editingTagItemVisibleInfo = visibleItems.find { it.index == index }
                            val isEditTagItemFullyVisible = editingTagItemVisibleInfo != null
                            Timber.tag("RTRT").d("startOffset:${lazyListState.layoutInfo.viewportStartOffset}")
                            Timber.tag("RTRT").d("endOffset:${lazyListState.layoutInfo.viewportEndOffset}")
                            Timber.tag("RTRT")
                                .d("editingTagItemVisibleInfo offset:${editingTagItemVisibleInfo?.offset}")
                            Timber.tag("RTRT").d("editingTagItemVisibleInfo size:${editingTagItemVisibleInfo?.size}")

                            if (!isEditTagItemFullyVisible) {
                                lazyListState.scrollToItem(index)
                            }
                        }
                    }
                )
            }
        }

        val tagToDelete = openDialog.value
        if (tagToDelete.isPresent) {
            AlertDialog(
                shape = RoundedCornerShape(dimensionResource(R.dimen.base_corner_radius)),
                backgroundColor = colorResource(R.color.bottomSheetBackgroundColor),
                onDismissRequest = { openDialog.value = Optional.empty() },
                title = { Text(stringResource(R.string.delete_tag_title)) },
                text = { Text(stringResource(R.string.delete_tag_description)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onTagRemoveClicked(tagToDelete.get())
                            openDialog.value = Optional.empty()
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
}

//@ExperimentalComposeUiApi
//@Preview
//@Composable
//fun EditTagsScreenPreview() {
//    EditTagsScreen(
//        { }
//    )
//}