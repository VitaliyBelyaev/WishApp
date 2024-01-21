package ru.vitaliy.belyaev.wishapp.ui.screens.wish_images

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
fun WishImagesViewerScreenRoute(
    onBackPressed: () -> Unit,
    viewModel: WishImagesViewerViewModel = hiltViewModel(),
    appViewModel: AppActivityViewModel = hiltViewModel(LocalContext.current as AppActivity),
) {

    val images: List<ImageEntity> by viewModel.images.collectAsState()

    WishImagesViewerScreen(
        onBackPressed = onBackPressed,
        initialWishImageIndex = viewModel.initialWishImageIndex,
        images = images,
        onDeleteImageConfirmed = appViewModel::onDeleteWishImageConfirmed,
    )
}