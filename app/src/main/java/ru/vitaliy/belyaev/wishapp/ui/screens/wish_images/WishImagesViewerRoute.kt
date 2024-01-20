package ru.vitaliy.belyaev.wishapp.ui.screens.wish_images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity

@Composable
fun WishImagesViewerScreenRoute(
    onBackPressed: () -> Unit,
    viewModel: WishImagesViewerViewModel = hiltViewModel()
) {

    val images: List<ImageEntity> by viewModel.images.collectAsState()

    WishImagesViewerScreen(
        onBackPressed = onBackPressed,
        initialWishImageIndex = viewModel.initialWishImageIndex,
        images = images,
        onDeleteImageConfirmed = viewModel::onDeleteImageConfirmed,
    )
}