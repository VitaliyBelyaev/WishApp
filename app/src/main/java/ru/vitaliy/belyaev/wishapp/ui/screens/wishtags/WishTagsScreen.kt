package ru.vitaliy.belyaev.wishapp.ui.screens.wishtags

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.ScrollAwareTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.components.AddTagBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.components.TagItemBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.entity.TagItem

@Composable
fun WishTagsScreen(
    onBackPressed: () -> Unit,
    viewModel: WishTagsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var query: String by remember { mutableStateOf("") }
    val state: List<TagItem> by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            ScrollAwareTopBar(
                title = {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
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
                        ),
                        trailingIcon = {
                            if (query.isNotBlank()) {
                                val shape = RoundedCornerShape(50.dp)
                                ThemedIcon(
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
                        ThemedIcon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Divider()

        LazyColumn {
            if (query.isNotBlank()) {
                item {
                    AddTagBlock(
                        tagName = query,
                        onClick = {
                            query = ""
                            viewModel.onQueryChanged("")
                            viewModel.onAddTagClicked(it)
                        }
                    )
                }
            }
            items(state) { tagItem ->
                TagItemBlock(tagItem = tagItem, onClick = { viewModel.onTagCheckboxClicked(it) })
            }
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