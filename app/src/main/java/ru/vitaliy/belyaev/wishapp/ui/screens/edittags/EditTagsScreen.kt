package ru.vitaliy.belyaev.wishapp.ui.screens.edittags

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.util.Optional
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.ui.core.alert_dialog.DestructiveConfirmationAlertDialog
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.components.EditTagBlock
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItem
import ru.vitaliy.belyaev.wishapp.ui.theme.CommonColors
import ru.vitaliy.belyaev.wishapp.utils.trackScreenShow

@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun EditTagsScreen(
    onBackPressed: () -> Unit,
    viewModel: EditTagsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val editTagItems: List<EditTagItem> by viewModel.uiState.collectAsStateWithLifecycle()
    val openDialog: MutableState<Optional<TagEntity>> = remember { mutableStateOf(Optional.empty()) }
    val lazyListState: LazyListState = rememberLazyListState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val handleBackPressed: () -> Unit = {
        keyboardController?.hide()
        onBackPressed()
    }

    BackHandler { handleBackPressed() }

    trackScreenShow { viewModel.trackScreenShow() }

    val systemUiController = rememberSystemUiController()
    val navBarColor = CommonColors.navBarColor()
    LaunchedEffect(key1 = Unit) {
        systemUiController.setNavigationBarColor(
            color = navBarColor,
        )
    }

    val onTagClick: (TagEntity) -> Unit = {
        viewModel.onTagClicked(it)
    }
    val onEditDoneClick: (String, TagEntity) -> Unit = { newTitle, tag ->
        viewModel.onEditTagDoneClicked(newTitle, tag)
    }
    val onRemoveClick: (TagEntity) -> Unit = {
        openDialog.value = Optional.of(it)
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.Companion.safeDrawing,
        topBar = {
            WishAppTopBar(
                title = stringResource(R.string.edit_tags),
                withBackIcon = true,
                onBackPressed = handleBackPressed,
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { pd ->

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.padding(pd)
        ) {
            items(editTagItems) { editTagItem ->
                EditTagBlock(
                    editTagItem = editTagItem,
                    onClick = onTagClick,
                    onRemoveClick = onRemoveClick,
                    onEditDoneClick = onEditDoneClick,
                )
            }
        }

        val tagToDelete = openDialog.value
        if (tagToDelete.isPresent) {
            DestructiveConfirmationAlertDialog(
                onDismissRequest = { openDialog.value = Optional.empty() },
                title = { Text(stringResource(R.string.delete_tag_title)) },
                text = { Text(stringResource(R.string.delete_tag_description)) },
                confirmClick = {
                    viewModel.onTagRemoveClicked(tagToDelete.get())
                    openDialog.value = Optional.empty()
                },
            )
        }
    }
}

//@ExperimentalComposeUiApi
//@Preview
//@Composable
//fun EditTagsScreenPreview() {
//    EditTagsScreen(
//        { }
//    )
//}