package ru.vitaliy.belyaev.wishapp.ui.screens.wish_images

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
fun WishImagesViewerScreenRoute(
    wishId: String,
    initialWishImageIndex: Int,
    onBackPressed: () -> Unit,
    viewModel: WishImagesViewerViewModel =
        hiltViewModel<WishImagesViewerViewModel, WishImagesViewerViewModel.Factory> { factory ->
            factory.create(wishId)
        },
    appViewModel: AppActivityViewModel = hiltViewModel(LocalActivity.current as AppActivity),
) {
    val images: List<ImageEntity> by viewModel.images.collectAsStateWithLifecycle()

    WishImagesViewerScreen(
        onBackPressed = onBackPressed,
        initialWishImageIndex = initialWishImageIndex,
        images = images,
        onDeleteImageConfirmed = appViewModel::onDeleteWishImageConfirmed,
    )
}