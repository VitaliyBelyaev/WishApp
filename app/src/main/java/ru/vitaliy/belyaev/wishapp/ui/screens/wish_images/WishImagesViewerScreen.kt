package ru.vitaliy.belyaev.wishapp.ui.screens.wish_images

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.util.Optional
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.ui.core.alert_dialog.DestructiveConfirmationAlertDialog
import ru.vitaliy.belyaev.wishapp.ui.core.dropdown.DeleteDropDownItem
import ru.vitaliy.belyaev.wishapp.ui.core.dropdown.MenuMoreWithDropDown
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.theme.CommonColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WishImagesViewerScreen(
    onBackPressed: () -> Unit,
    initialWishImageIndex: Int,
    images: List<ImageEntity>,
    onDeleteImageConfirmed: (imageIndex: Int) -> Unit,
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState: ScrollState = rememberScrollState()

//    trackScreenShow { viewModel.trackScreenShow() }

    val systemUiController = rememberSystemUiController()
    val screenNavBarColor = CommonColors.navBarColor()
    LaunchedEffect(key1 = Unit) {
        systemUiController.setNavigationBarColor(color = screenNavBarColor)
    }

    val openDeleteImageConfirmationDialog: MutableState<Optional<Int>> =
        remember { mutableStateOf(Optional.empty()) }

    val pagerState = rememberPagerState(
        initialPage = initialWishImageIndex,
        pageCount = { images.size },
    )

    val currentPageNumber = pagerState.currentPage + 1
    val title = images.takeIf { it.isNotEmpty() }?.let {
        "$currentPageNumber / ${it.size}"
    } ?: ""

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            WishAppTopBar(
                title = title,
                withBackIcon = true,
                onBackPressed = onBackPressed,
                actions = {
                    MenuMoreWithDropDown { expanded ->
                        DeleteDropDownItem {
                            expanded.value = false
                            openDeleteImageConfirmationDialog.value = Optional.of(pagerState.currentPage)
//                            onDeleteImageClicked(pagerState.currentPage)
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->

        HorizontalPager(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            state = pagerState,
            beyondBoundsPageCount = 1,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapVelocityThreshold = 600.dp
            ),
        ) { page ->
            ZoomableContent1(
                page = page,
                pagerState = pagerState,
                images = images,
            )
        }
    }

    val imageIndexToDelete = openDeleteImageConfirmationDialog.value
    if (imageIndexToDelete.isPresent) {
        DestructiveConfirmationAlertDialog(
            onDismissRequest = { openDeleteImageConfirmationDialog.value = Optional.empty() },
            title = { Text(stringResource(R.string.delete_wish_title)) },
            confirmClick = {
                openDeleteImageConfirmationDialog.value = Optional.empty()
                onDeleteImageConfirmed(imageIndexToDelete.get())
//                onBackPressed()
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagerScope.ZoomableContent1(
    page: Int,
    pagerState: PagerState,
    images: List<ImageEntity>,
) {
    val isActivePage = page == pagerState.settledPage

    val zoomableState = rememberZoomableState(
        zoomSpec = ZoomSpec(
            maxZoomFactor = 3f,
            preventOverOrUnderZoom = true
        ),
        autoApplyTransformations = true
    )
    images.getOrNull(page)?.let { image ->
        ZoomableAsyncImage(
            modifier = Modifier.fillMaxSize(),
            state = rememberZoomableImageState(zoomableState),
            model = image.rawData,
            contentDescription = null,
        )
    }

    if (!isActivePage) {
        LaunchedEffect(Unit) {
            zoomableState.resetZoom(withAnimation = false)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagerScope.ZoomableContent2(
    page: Int,
    pagerState: PagerState,
    images: List<ImageEntity>
) {
    val isActivePage = page == pagerState.settledPage

    val zoomState = rememberZoomState(
        maxScale = 3f,
        velocityDecay = exponentialDecay(
            frictionMultiplier = 1f,
            absVelocityThreshold = 0.1f
        )
    )
    images.getOrNull(page)?.let { image ->
        AsyncImage(
            modifier = Modifier
                .zoomable(zoomState)
                .fillMaxSize(),
            model = image.rawData,
            contentDescription = null,
        )
    }

    if (!isActivePage) {
        LaunchedEffect(Unit) {
            zoomState.reset()
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000fd0,
    showSystemUi = true,
)
@Composable
private fun WishImagesViewerScreenPreview() {

    val images = listOf(
        ImageEntity("1", "1", ByteArray(0)),
        ImageEntity("2", "1", ByteArray(0)),
        ImageEntity("3", "1", ByteArray(0)),
    )

    WishImagesViewerScreen(
        onBackPressed = {},
        initialWishImageIndex = 0,
        images = images,
        onDeleteImageConfirmed = {},
    )
}