package ru.vitaliy.belyaev.wishapp.ui.screens.edittags

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.*
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.components.RemoveTagBlock

@Composable
fun EditTagsScreen(
    onBackPressed: () -> Unit,
    viewModel: EditTagsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val state: List<Tag> by viewModel.uiState.collectAsState()
    val openDialog: MutableState<Optional<Tag>> = remember { mutableStateOf(Optional.empty()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.delete_tag)) },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed.invoke() }) {
                        ThemedIcon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = if (MaterialTheme.colors.isLight) AppBarDefaults.TopAppBarElevation else 0.dp
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {

        LazyColumn {
            items(state) { tag ->
                RemoveTagBlock(
                    tag = tag,
                    onRemoveClick = {
                        openDialog.value = Optional.of(it)
                    }
                )
            }
        }

        val tagToDelete = openDialog.value
        if (tagToDelete.isPresent) {

            AlertDialog(
                shape = RoundedCornerShape(dimensionResource(R.dimen.base_corner_radius)),
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

@Preview
@Composable
fun EditTagsScreenPreview() {
    EditTagsScreen(
        { }
    )
}