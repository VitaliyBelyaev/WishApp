package ru.vitaliy.belyaev.wishapp.ui.screens.edittags

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.*
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.components.EditTagBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItem

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
            items(editTagItems) { editTagItem ->
                EditTagBlock(
                    editTagItem = editTagItem,
                    onClick = { viewModel.onTagClicked(it) },
                    onRemoveClick = {
                        openDialog.value = Optional.of(it)
                    },
                    onEditDoneClick = { viewModel.onEditTagDoneClicked(it, editTagItem.tag) }
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

@ExperimentalComposeUiApi
@Preview
@Composable
fun EditTagsScreenPreview() {
    EditTagsScreen(
        { }
    )
}