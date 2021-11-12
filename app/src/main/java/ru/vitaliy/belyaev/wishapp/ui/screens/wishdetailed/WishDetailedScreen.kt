package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Optional
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.toValueOfNull
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.core.LinkPreview
import ru.vitaliy.belyaev.wishapp.ui.core.linkpreview.LinkPreviewLoading
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.Data
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.Loading
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.None
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishItem

@ExperimentalCoroutinesApi
@Composable
fun WishDetailedScreen(
    onBackPressed: () -> Unit,
    appViewModel: AppActivityViewModel = hiltViewModel(LocalContext.current as AppActivity),
    viewModel: WishDetailedViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val wishItem: Optional<WishItem> by viewModel.uiState.collectAsState()
    val handleBackPressed: () -> Unit = {
        viewModel.onBackPressed()
        onBackPressed()
        appViewModel.onWishScreenExit(viewModel.wishId)
    }

    BackHandler { handleBackPressed() }

    Scaffold(
        topBar = {
            WishAppTopBar(
                "",
                withBackIcon = true,
                onBackPressed = handleBackPressed,
                actions = {
                    IconButton(onClick = {
                        viewModel.onDeleteWishClicked()
                        onBackPressed()
                        appViewModel.onDeleteWishClicked(viewModel.wishId)
                    }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete wish"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        if (!wishItem.isPresent) {
            return@Scaffold
        }
        var title: String by remember { mutableStateOf(wishItem.valueOrEmptyString { it.wish.title }) }
        var link: String by remember { mutableStateOf(wishItem.valueOrEmptyString { it.wish.link }) }
        var comment: String by remember { mutableStateOf(wishItem.valueOrEmptyString { it.wish.comment }) }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            TextField(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                value = title,
                textStyle = MaterialTheme.typography.h6,
                onValueChange = { newValue ->
                    title = newValue
                    viewModel.onWishTitleChanged(newValue)
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.enter_title),
                        style = MaterialTheme.typography.h6,
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White
                )
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = comment,
                onValueChange = { newValue ->
                    comment = newValue
                    viewModel.onWishCommentChanged(newValue)
                },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_notes),
                        contentDescription = "Comment",
                        tint = Color.DarkGray
                    )
                },
                placeholder = { Text(text = stringResource(R.string.enter_comment)) },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White
                )
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = link,
                onValueChange = { newValue ->
                    link = newValue
                    viewModel.onWishLinkChanged(newValue)
                },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_link),
                        contentDescription = "Link",
                        tint = Color.DarkGray
                    )
                },
                placeholder = { Text(text = stringResource(R.string.enter_link)) },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White
                )
            )

            val wishItemValue = wishItem.toValueOfNull()
            when (val linkPreviewState = wishItemValue?.linkPreviewState) {
                is Data -> {
                    val pd = PaddingValues(start = 12.dp, top = 8.dp, end = 12.dp)
                    LinkPreview(pd, linkPreviewState.linkInfo)
                }
                is Loading -> {
                    val pd = PaddingValues(start = 12.dp, top = 8.dp, end = 12.dp)
                    LinkPreviewLoading(pd)
                }
                is None -> {
                    //nothing
                }
            }
        }
    }
}

private fun <T> Optional<T>.valueOrEmptyString(extractor: (T) -> String): String =
    if (isPresent) {
        extractor(get())
    } else {
        ""
    }

@ExperimentalCoroutinesApi
@Preview
@Composable
fun EditWishScreenPreview() {
    WishDetailedScreen(
        { }
    )
}