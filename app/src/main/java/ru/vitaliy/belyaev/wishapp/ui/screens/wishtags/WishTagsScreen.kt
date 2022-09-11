package ru.vitaliy.belyaev.wishapp.ui.screens.wishtags

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.navigationBarsWithImePadding
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.ScrollAwareTopAppBar
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.components.AddTagBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.components.TagItemBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.entity.TagItem

@ExperimentalComposeUiApi
@Composable
fun WishTagsScreen(
    onBackPressed: () -> Unit,
    viewModel: WishTagsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var query: String by remember { mutableStateOf("") }
    val tagItems: List<TagItem> by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val handleBackPressed: () -> Unit = {
        keyboardController?.hide()
        onBackPressed()
    }

    BackHandler { handleBackPressed() }

    val onAddTagRequested: () -> Unit = {
        val tagName = query
        if (tagName.isNotBlank()) {
            query = ""
            viewModel.onQueryChanged("")
            viewModel.onAddTagClicked(tagName)
        } else {
            keyboardController?.hide()
        }
    }

    Scaffold(
        topBar = {
            ScrollAwareTopAppBar(
                title = {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        value = query,
                        singleLine = true,
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
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { onAddTagRequested() }),
                        trailingIcon = {
                            if (query.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        query = ""
                                        viewModel.onQueryChanged(query)
                                    }
                                ) {
                                    ThemedIcon(
                                        Icons.Filled.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBackPressed) {
                        ThemedIcon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.navigationBarsWithImePadding()
    ) {
        Divider()

        LazyColumn(modifier = Modifier.padding(it)) {
            val showAddTagBlock = query.isNotBlank() && tagItems.none { it.tag.title == query }
            if (showAddTagBlock) {
                item {
                    AddTagBlock(
                        tagName = query,
                        onClick = { onAddTagRequested() }
                    )
                }
            }
            items(tagItems) { tagItem ->
                TagItemBlock(tagItem = tagItem, onClick = { viewModel.onTagCheckboxClicked(it) })
            }
        }
    }
}

@ExperimentalComposeUiApi
@Preview
@Composable
fun WishTagsScreenPreview() {
    WishTagsScreen(
        { }
    )
}