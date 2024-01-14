package ru.vitaliy.belyaev.wishapp.ui.screens.wish_images

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.theme.CommonColors
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@Composable
fun WishImagesViewerScreen(
    onBackPressed: () -> Unit,
    viewModel: WishImagesViewerViewModel = hiltViewModel()
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

    val images: List<ImageEntity> by viewModel.images.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = viewModel.initialWishImageIndex,
        pageCount = { images.size },
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            val title = images.takeIf { it.isNotEmpty() }?.let { images ->
                "${pagerState.currentPage + 1}/${images.size}"
            } ?: ""
            WishAppTopBar(
                title = title,
                withBackIcon = true,
                onBackPressed = onBackPressed,
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
//            pageSpacing = 16.dp,
            key = { index -> images.getOrNull(index)?.id ?: "" }
        ) { page ->

            val isActivePage = page == pagerState.settledPage

            val zoomableState = rememberZoomableState()
            Timber.tag("RTRT").d("page: $page")
            images.getOrNull(page)?.let { image ->
                Timber.tag("RTRT").d("WishImagesViewerScreen: image: ${image.id}")
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
    }
}