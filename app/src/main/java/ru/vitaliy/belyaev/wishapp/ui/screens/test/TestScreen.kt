package ru.vitaliy.belyaev.wishapp.ui.screens.test

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.navigationBarsPadding
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.theme.localTheme
import ru.vitaliy.belyaev.wishapp.utils.isScrollInInitialState
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TestScreen(
    onBackPressed: () -> Unit,
    viewModel: TestViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val items: List<TestItem> by viewModel.uiState.collectAsState()

    val onClicked: (TestItem) -> Unit = {}
    val onLongClicked: (TestItem) -> Unit = {}

    val reorderableLazyListState = rememberReorderableLazyListState(onMove = { from, to ->
        Timber.tag("RTRT").d("from:$from, to:$to")

        viewModel.onMove(from, to)
    })


    Scaffold(
        topBar = {
            WishAppTopBar(
                "Test screen",
                withBackIcon = true,
                onBackPressed = onBackPressed,
                isScrollInInitialState = { reorderableLazyListState.listState.isScrollInInitialState() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.navigationBarsPadding()
    ) {

        LazyColumn(
            state = reorderableLazyListState.listState,
            modifier = Modifier
                .padding(it)
                .reorderable(reorderableLazyListState)
                .detectReorderAfterLongPress(reorderableLazyListState)
        ) {

            items(items, { item -> item.id }) { testItem ->

                ReorderableItem(reorderableState = reorderableLazyListState, key = testItem.id) { isDragging ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        val backgroundColor: Color = Color.Transparent
                        val baseShape = RoundedCornerShape(dimensionResource(R.dimen.base_corner_radius))
                        val borderWidth = 1.dp

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = backgroundColor, shape = baseShape)
                                .border(borderWidth, localTheme.colors.iconPrimaryColor, baseShape)
                                .clip(baseShape)
//                                .combinedClickable(
//                                    onLongClick = { onLongClicked.invoke(testItem) },
//                                    onClick = { onClicked.invoke(testItem) }
//                                )
                                .padding(14.dp)
                        ) {
                            val (title, titleColor) = if (testItem.title.isNotBlank()) {
                                testItem.title to Color.Unspecified
                            } else {
                                stringResource(R.string.without_title) to Color.Gray
                            }

                            Text(
                                text = title,
                                color = titleColor,
                                style = MaterialTheme.typography.h6,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}