package ru.vitaliy.belyaev.wishapp.ui.screens.modifywish

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Optional
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar

@Composable
fun ModifyWishScreen(
    onBackPressed: () -> Unit,
    viewModel: ModifyWishViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val wish: Optional<Wish> by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            WishAppTopBar(
                "",
                withBackIcon = true,
                onBackPressed = onBackPressed
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        var title: String by remember { mutableStateOf("") }
        var link: String by remember { mutableStateOf("") }
        var comment: String by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .padding(
                    PaddingValues(
                        start = 16.dp,
                        top = paddingValues.calculateTopPadding(),
                        end = 16.dp,
                        bottom = paddingValues.calculateBottomPadding()
                    )
                )
                .verticalScroll(scrollState)
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = wish.valueOrEmptyString { it.title },
                onValueChange = { value -> title = value },
                placeholder = { Text(text = stringResource(R.string.enter_title)) },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White
                )
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = wish.valueOrEmptyString { it.link },
                onValueChange = { value -> link = value },
                placeholder = { Text(text = stringResource(R.string.enter_link)) },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White
                )
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = wish.valueOrEmptyString { it.comment ?: "" },
                onValueChange = { value -> comment = value },
                placeholder = { Text(text = stringResource(R.string.enter_comment)) },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White
                )
            )
        }
    }
}

private fun <T> Optional<T>.valueOrEmptyString(extractor: (T) -> String): String =
    if (isPresent) {
        extractor(get())
    } else {
        ""
    }

@Preview
@Composable
fun EditWishScreenPreview() {
    ModifyWishScreen(
        {}
    )
}