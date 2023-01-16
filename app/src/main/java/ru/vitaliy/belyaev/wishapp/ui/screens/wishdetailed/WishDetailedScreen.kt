package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.util.Optional
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.toValueOfNull
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.linkpreview.LinkPreview
import ru.vitaliy.belyaev.wishapp.ui.core.linkpreview.LinkPreviewLoading
import ru.vitaliy.belyaev.wishapp.ui.core.tags.TagsBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.Data
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.LinkInfo
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.Loading
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.NoData
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.None
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishItem
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Composable
fun WishDetailedScreen(
    onBackPressed: () -> Unit,
    onWishTagsClicked: (String) -> Unit,
    appViewModel: AppActivityViewModel = hiltViewModel(LocalContext.current as AppActivity),
    viewModel: WishDetailedViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val wishItem: Optional<WishItem> by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val handleBackPressed: () -> Unit = {
        keyboardController?.hide()
        viewModel.onBackPressed()
        appViewModel.onWishScreenExit(viewModel.wishId, viewModel.inputWishId.isBlank())
        onBackPressed()
    }
    val openDialog: MutableState<Optional<WishItem>> = remember { mutableStateOf(Optional.empty()) }
    val scrollState: ScrollState = rememberScrollState()
    val systemUiController = rememberSystemUiController()

    BackHandler { handleBackPressed() }

    val onLinkPreviewClick: (String) -> Unit = { url ->
        viewModel.onLinkPreviewClick()
        try {
            uriHandler.openUri(url)
        } catch (error: Throwable) {
            Timber.e(error)
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.fail_to_open_link))
            }
        }
    }

    val screenNavBarColor = MaterialTheme.colorScheme.surfaceColorAtElevation(BottomAppBarDefaults.ContainerElevation)
    LaunchedEffect(key1 = Unit) {
        systemUiController.setNavigationBarColor(
            color = screenNavBarColor
        )
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            WishDetailedTopBar(
                onBackPressed = handleBackPressed,
                wishItem = wishItem.toValueOfNull(),
                onWishTagsClicked = onWishTagsClicked,
                onDeleteClicked = { openDialog.value = wishItem },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.Companion.safeDrawing,
    ) { paddingValues ->
        if (!wishItem.isPresent) {
            return@Scaffold
        }
        var title: String by remember { mutableStateOf(wishItem.valueOrEmptyString { it.wish.title }) }
        var link: String by remember { mutableStateOf(wishItem.valueOrEmptyString { it.wish.link }) }
        var comment: String by remember { mutableStateOf(wishItem.valueOrEmptyString { it.wish.comment }) }
        val isCompleted: Boolean = wishItem.toValueOfNull()?.wish?.isCompleted ?: false

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val (contentRef, bottomPanelRef) = createRefs()

            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .constrainAs(contentRef) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(bottomPanelRef.top)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                    }
            ) {
                val focusRequester = remember { FocusRequester() }
                TextField(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = title,
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                    ),
                    onValueChange = { newValue ->
                        title = newValue
                        viewModel.onWishTitleChanged(newValue)
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.enter_title),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                DisposableEffect(title) {
                    if (title.isBlank()) {
                        focusRequester.requestFocus()
                    }
                    onDispose { }
                }

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = comment,
                    onValueChange = { newValue ->
                        comment = newValue
                        viewModel.onWishCommentChanged(newValue)
                    },
                    leadingIcon = {
                        ThemedIcon(
                            painterResource(R.drawable.ic_notes),
                            contentDescription = "Comment"
                        )
                    },
                    placeholder = { Text(text = stringResource(R.string.enter_comment)) },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
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
                        ThemedIcon(
                            painterResource(R.drawable.ic_link),
                            contentDescription = "Link"
                        )
                    },
                    placeholder = { Text(text = stringResource(R.string.enter_link)) },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                val wishItemValue = wishItem.toValueOfNull()
                val pd = PaddingValues(start = 12.dp, end = 12.dp)
                when (val linkPreviewState = wishItemValue?.linkPreviewState) {
                    is Data -> {
                        LinkPreview(
                            linkInfo = linkPreviewState.linkInfo,
                            url = wishItemValue.wish.link,
                            paddingValues = pd,
                            onLinkPreviewClick = onLinkPreviewClick,
                        )
                    }

                    is Loading -> {
                        LinkPreviewLoading(pd)
                    }

                    is NoData -> {
                        LinkPreview(
                            linkInfo = LinkInfo(title = stringResource(R.string.open_link)),
                            url = wishItemValue.wish.link,
                            paddingValues = pd,
                            onLinkPreviewClick = onLinkPreviewClick,
                        )
                    }

                    is None -> {
                        //nothing
                    }

                    else -> {
                        //nothing
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                val tags = wishItem.toValueOfNull()?.wish?.tags ?: emptyList()
                TagsBlock(
                    tags = tags,
                    textSize = 16.sp,
                    onClick = {
                        val wishId = wishItem.toValueOfNull()?.wish?.id ?: return@TagsBlock
                        onWishTagsClicked(wishId)
                    },
                    onAddNewTagClick = {
                        val wishId = wishItem.toValueOfNull()?.wish?.id ?: return@TagsBlock
                        onWishTagsClicked(wishId)
                    },
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            val text = if (isCompleted) {
                stringResource(R.string.wish_not_done)
            } else {
                stringResource(R.string.wish_done)
            }
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = BottomAppBarDefaults.ContainerElevation,
                modifier = Modifier
                    .constrainAs(bottomPanelRef) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                        height = Dimension.value(56.dp)
                    }
            ) {
                Box {
                    TextButton(
                        onClick = {
                            val wishId = wishItem.toValueOfNull()?.wish?.id ?: return@TextButton
                            appViewModel.onCompleteWishButtonClicked(
                                wishId = wishId,
                                oldIsCompleted = isCompleted
                            )
                            if (!isCompleted) {
                                appViewModel.showSnackMessageOnMain(context.getString(R.string.wish_done_snack_message))
                                onBackPressed()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }

    val wishToDelete = openDialog.value
    if (wishToDelete.isPresent) {
        AlertDialog(
            onDismissRequest = { openDialog.value = Optional.empty() },
            title = { Text(stringResource(R.string.delete_wish_title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = Optional.empty()
                        viewModel.onDeleteWishClicked()
                        appViewModel.onDeleteWishClicked(viewModel.wishId)
                        onBackPressed()
                    }
                ) {
                    Text(
                        stringResource(R.string.delete)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { openDialog.value = Optional.empty() }
                ) {
                    Text(
                        stringResource(R.string.cancel),
                    )
                }
            }
        )
    }
}

private fun <T> Optional<T>.valueOrEmptyString(extractor: (T) -> String): String =
    if (isPresent) {
        extractor(get())
    } else {
        ""
    }

//@ExperimentalMaterialApi
//@ExperimentalCoroutinesApi
//@Preview
//@Composable
//fun WishDetailedScreenPreview() {
//    WishDetailedScreen(
//        { }
//    )
//}