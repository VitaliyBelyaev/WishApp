package ru.vitaliy.belyaev.wishapp.ui.screens.wishtags

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.components.AddTagBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.components.TagItemBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.entity.TagItem
import ru.vitaliy.belyaev.wishapp.ui.theme.CommonColors
import ru.vitaliy.belyaev.wishapp.utils.trackScreenShow

@OptIn(ExperimentalMaterial3Api::class)
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

    trackScreenShow { viewModel.trackScreenShow() }

    val systemUiController = rememberSystemUiController()
    val screenNavBarColor = CommonColors.navBarColor()
    LaunchedEffect(key1 = Unit) {
        systemUiController.setNavigationBarColor(color = screenNavBarColor)
    }

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

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        value = query,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        onValueChange = { newValue ->
                            query = newValue
                            viewModel.onQueryChanged(newValue)
                        },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.enter_label_name),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
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
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                },
                navigationIcon = {
                    IconButton(onClick = handleBackPressed) {
                        ThemedIcon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.Companion.safeDrawing
    ) { paddingValues ->

        LazyColumn(modifier = Modifier.padding(paddingValues)) {
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