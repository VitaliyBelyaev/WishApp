package ru.vitaliy.belyaev.wishapp.ui.screens.edittags

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import java.util.Optional
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.database.Tag
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.components.EditTagBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItem
import ru.vitaliy.belyaev.wishapp.ui.theme.localTheme
import ru.vitaliy.belyaev.wishapp.utils.isScrollInInitialState

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

    val onTagClick: (Tag) -> Unit = {
        viewModel.onTagClicked(it)
    }
    val onEditDoneClick: (String, Tag) -> Unit = { newTitle, tag ->
        viewModel.onEditTagDoneClicked(newTitle, tag)
    }
    val onRemoveClick: (Tag) -> Unit = {
        openDialog.value = Optional.of(it)
    }
    Scaffold(
        topBar = {
            WishAppTopBar(
                title = stringResource(R.string.edit_tags),
                withBackIcon = true,
                onBackPressed = handleBackPressed,
                isScrollInInitialState = { lazyListState.isScrollInInitialState() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.navigationBarsWithImePadding()
    ) {

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.padding(it)
        ) {
            itemsIndexed(editTagItems) { index, editTagItem ->
                EditTagBlock(
                    editTagItem = editTagItem,
                    onClick = onTagClick,
                    onRemoveClick = onRemoveClick,
                    onEditDoneClick = onEditDoneClick,
                )
            }
        }

        val focusedTag = editTagItems.find { it.isEditMode }
        if (focusedTag != null) {
            val insets = LocalWindowInsets.current
            val isImeVisible = insets.ime.isVisible
            val focusedTagIndex = editTagItems.indexOf(focusedTag)
            val isEditTagItemFullyVisible = isEditTagItemFullyVisible(lazyListState, focusedTagIndex)

            if (isImeVisible && !isEditTagItemFullyVisible) {
                SideEffect {
                    with(lazyListState.layoutInfo) {
                        val itemSize = visibleItemsInfo.first().size
                        val itemScrollOffset = viewportEndOffset - itemSize
                        coroutineScope.launch {
                            lazyListState.scrollToItem(focusedTagIndex, -itemScrollOffset)
                        }
                    }
                }
            }
        }

        val tagToDelete = openDialog.value
        if (tagToDelete.isPresent) {
            AlertDialog(
                shape = RoundedCornerShape(dimensionResource(R.dimen.base_corner_radius)),
                backgroundColor = localTheme.colors.bottomSheetBackgroundColor,
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

private fun isEditTagItemFullyVisible(lazyListState: LazyListState, editTagItemIndex: Int): Boolean {
    with(lazyListState.layoutInfo) {
        val editingTagItemVisibleInfo = visibleItemsInfo.find { it.index == editTagItemIndex }
        return if (editingTagItemVisibleInfo == null) {
            false
        } else {
            viewportEndOffset - editingTagItemVisibleInfo.offset >= editingTagItemVisibleInfo.size
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