package ru.vitaliy.belyaev.wishapp.ui.screens.edittags

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.components.EditTagItemBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItem

@Composable
fun EditTagsScreen(
    onBackPressed: () -> Unit,
    viewModel: EditTagsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var query: String by remember { mutableStateOf("") }
    val state: List<EditTagItem> by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.edit_tags)) },
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
            items(state) { editTagItem ->
                EditTagItemBlock(
                    editTagItem = editTagItem,
                    onClick = { viewModel.onEditTagItemClicked(it) },
                    onAddClick = { viewModel.onAddTagClicked(it) },
                    onRemoveClick = { viewModel.onTagRemoveClicked(it) },
                    onEditDoneClick = { viewModel.onEditDone(it) }
                )
            }
        }
    }
}

@Preview
@Composable
fun EditTagsScreenPreview() {
    EditTagsScreen(
        { }
    )
}