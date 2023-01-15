package ru.vitaliy.belyaev.wishapp.ui.screens.edittags

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Optional
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.database.Tag
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.components.EditTagBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItem

@ExperimentalMaterial3Api
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
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.Companion.safeContent
    ) { pd ->

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.padding(pd)
        ) {
            items(editTagItems) { editTagItem ->
                EditTagBlock(
                    editTagItem = editTagItem,
                    onClick = onTagClick,
                    onRemoveClick = onRemoveClick,
                    onEditDoneClick = onEditDoneClick,
                )
            }
        }

        val tagToDelete = openDialog.value
        if (tagToDelete.isPresent) {
            AlertDialog(
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
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { openDialog.value = Optional.empty() }
                    ) {
                        Text(stringResource(R.string.cancel))
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