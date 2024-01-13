package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.util.Optional
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.core.alert_dialog.DestructiveConfirmationAlertDialog
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.core.tags.TagsBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.WishItem
import ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed.components.WishDetailedBottomBar
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppTextFieldColors
import ru.vitaliy.belyaev.wishapp.utils.showDismissableSnackbar
import ru.vitaliy.belyaev.wishapp.utils.toValueOfNull
import ru.vitaliy.belyaev.wishapp.utils.trackScreenShow
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val openDeleteWishConfirmationDialog: MutableState<Optional<WishItem>> =
        remember { mutableStateOf(Optional.empty()) }
    val openDeleteLinkConfirmationDialog: MutableState<Optional<String>> = remember { mutableStateOf(Optional.empty()) }
    val scrollState: ScrollState = rememberScrollState()
    val systemUiController = rememberSystemUiController()
    val bottomBarHeight = 56.dp

    val openLink: (String) -> Unit = { link ->
        try {
            uriHandler.openUri(link)
        } catch (error: Throwable) {
            Timber.e(error)
            scope.launch {
                snackbarHostState.showDismissableSnackbar(context.getString(R.string.fail_to_open_link))
            }
        }
    }

    val images: List<ImageEntity> by viewModel.images.collectAsState()

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    viewModel.onImageSelected(bytes)
                }
            }
        }
    )

    val maxSelectionCount = 1
    // I will start this off by saying that I am still learning Android development:
    // We are tricking the multiple photos picker here which is probably not the best way,
    // if you know of a better way to implement this feature drop a comment and let me know
    // how to improve this design
//    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxSelectionCount),
//        onResult = { uris ->  }
//    )

    fun launchPhotoPicker() {
        singlePhotoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )

//        if (maxSelectionCount > 1) {
//            multiplePhotoPickerLauncher.launch(
//                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//            )
//        } else {
//            singlePhotoPickerLauncher.launch(
//                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//            )
//        }
    }

    BackHandler { handleBackPressed() }

    trackScreenShow { viewModel.trackScreenShow() }

    val screenNavBarColor = MaterialTheme.colorScheme.surfaceColorAtElevation(BottomAppBarDefaults.ContainerElevation)
    systemUiController.setNavigationBarColor(color = screenNavBarColor)

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            WishDetailedTopBar(
                onBackPressed = handleBackPressed,
                wishItem = wishItem.toValueOfNull(),
                onWishTagsClicked = onWishTagsClicked,
                onDeleteClicked = { openDeleteWishConfirmationDialog.value = wishItem },
                onAddImageClicked = { launchPhotoPicker() },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        bottomBar = {
            WishDetailedBottomBar(
                wishItem = wishItem.toValueOfNull(),
                onWishTagsClicked = onWishTagsClicked,
                onAddImageClicked = { launchPhotoPicker() },
                onWishCompletedClicked = { wishId, oldIsCompleted ->
                    appViewModel.onCompleteWishButtonClicked(
                        wishId = wishId,
                        oldIsCompleted = oldIsCompleted
                    )
                    if (!oldIsCompleted) {
                        appViewModel.showSnackMessageOnMain(context.getString(R.string.wish_done_snack_message))
                        onBackPressed()
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.padding(bottom = bottomBarHeight),
                hostState = snackbarHostState
            )
        },
        contentWindowInsets = WindowInsets.Companion.safeDrawing,
    ) { paddingValues ->
        if (!wishItem.isPresent) {
            return@Scaffold
        }
        var title: String by remember { mutableStateOf(wishItem.valueOrEmptyString { it.wish.title }) }
        var link: String by remember { mutableStateOf(viewModel.linkInputString) }
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
                        bottom.linkTo(parent.bottom)
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
                    colors = WishAppTextFieldColors.wishDetailedTextFieldColors(),
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
                    placeholder = { Text(text = stringResource(R.string.enter_comment)) },
                    colors = WishAppTextFieldColors.wishDetailedTextFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Divider()
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = link,
                    onValueChange = { newValue ->
                        link = newValue
                        viewModel.onWishLinkChanged(newValue)
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                viewModel.onAddLinkClicked(link)
                                link = ""
                                viewModel.onWishLinkChanged(link)
                            },
                            enabled = viewModel.isLinkValid(link)
                        ) {
                            ThemedIcon(
                                painterResource(R.drawable.ic_check),
                                contentDescription = "Add Link",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = LocalContentAlpha.current)
                            )
                        }
                    },
                    singleLine = true,
                    placeholder = { Text(text = stringResource(R.string.enter_link)) },
                    colors = WishAppTextFieldColors.wishDetailedTextFieldColors(),
                )
                Divider()

                for (linkItem in wishItem.toValueOfNull()?.wish?.links?.reversed() ?: emptyList()) {
                    val annotatedLinkString: AnnotatedString = buildAnnotatedString {
                        val linkHost: String = try {
                            Uri.parse(linkItem).host ?: linkItem
                        } catch (e: Exception) {
                            linkItem
                        }
                        append(linkHost)
                        addStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Medium,
                            ),
                            start = 0,
                            end = linkHost.length
                        )
                        addStringAnnotation(
                            tag = "URL",
                            annotation = linkItem,
                            start = 0,
                            end = linkHost.length
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(TextFieldDefaults.MinHeight)
                            .padding(start = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ClickableText(
                            style = LocalTextStyle.current,
                            text = annotatedLinkString,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            onClick = {
                                annotatedLinkString
                                    .getStringAnnotations("URL", it, it)
                                    .firstOrNull()?.let { stringAnnotation ->
                                        viewModel.onLinkClicked()
                                        openLink(stringAnnotation.item)
                                    }
                            }
                        )

                        IconButton(
                            onClick = {
                                viewModel.onDeleteLinkClicked()
                                openDeleteLinkConfirmationDialog.value = Optional.of(linkItem)
                            }
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_delete),
                                contentDescription = "Delete Link",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = LocalContentAlpha.current)
                            )
                        }
                    }
                    Divider()
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

                Spacer(modifier = Modifier.height(16.dp))

                if (images.isNotEmpty()) {
                    val itemsSpacing = 8.dp
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
                        verticalArrangement = Arrangement.spacedBy(itemsSpacing),
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp)
                    ) {
                        for (image in images) {
                            AsyncImage(
                                model = image.rawData,
                                contentDescription = null,
                                modifier = Modifier.height(100.dp),
                                contentScale = ContentScale.FillHeight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    val wishToDelete = openDeleteWishConfirmationDialog.value
    if (wishToDelete.isPresent) {
        DestructiveConfirmationAlertDialog(
            onDismissRequest = { openDeleteWishConfirmationDialog.value = Optional.empty() },
            title = { Text(stringResource(R.string.delete_wish_title)) },
            confirmClick = {
                openDeleteWishConfirmationDialog.value = Optional.empty()
                viewModel.onDeleteWishConfirmed()
                appViewModel.onDeleteWishConfirmed(viewModel.wishId)
                onBackPressed()
            },
        )
    }

    val linkToDeleteOptional = openDeleteLinkConfirmationDialog.value
    if (linkToDeleteOptional.isPresent) {
        DestructiveConfirmationAlertDialog(
            onDismissRequest = { openDeleteLinkConfirmationDialog.value = Optional.empty() },
            title = { Text(stringResource(R.string.delete_wish_link_title)) },
            text = { Text(linkToDeleteOptional.get()) },
            confirmClick = {
                openDeleteLinkConfirmationDialog.value = Optional.empty()
                viewModel.onDeleteWishLinkConfirmed(linkToDeleteOptional.get())
            },
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