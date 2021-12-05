package ru.vitaliy.belyaev.wishapp.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.WishTagsViewModel

@Composable
fun WishTagsScreen(
    onBackPressed: () -> Unit,
    viewModel: WishTagsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    var title: String by remember { mutableStateOf("") }
                    TextField(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        value = title,
                        textStyle = MaterialTheme.typography.h6,
                        onValueChange = { newValue ->
                            title = newValue
                            viewModel.onQueryChanged(newValue)
                        },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.enter_label_name),
                                style = MaterialTheme.typography.h6,
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = colorResource(R.color.inputCursorColor)
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed.invoke() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = colorResource(R.color.toolbarColor)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val scrollState = rememberScrollState()

    }
}

@Preview
@Composable
fun WishTagsScreenPreview() {
    WishTagsScreen(
        { }
    )
}