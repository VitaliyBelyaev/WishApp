package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar

@Composable
fun WishDetailedScreen(
    onBackPressed: () -> Unit,
    wishId: String = ""
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            WishAppTopBar(
                "WishDetailed",
                withBackIcon = true,
                onBackPressed = onBackPressed
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        val scrollState = rememberScrollState()
        var title: String by remember { mutableStateOf("") }
        var link: String by remember { mutableStateOf("") }
        var comment: String by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .padding(
                    PaddingValues(
                        start = 16.dp,
                        top = it.calculateTopPadding(),
                        end = 16.dp,
                        bottom = it.calculateBottomPadding()
                    )
                )
                .verticalScroll(scrollState)
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
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
                value = link,
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
                value = comment,
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

@Preview
@Composable
fun WishDetailedScreenPreview() {
    WishDetailedScreen(
        {}
    )
}