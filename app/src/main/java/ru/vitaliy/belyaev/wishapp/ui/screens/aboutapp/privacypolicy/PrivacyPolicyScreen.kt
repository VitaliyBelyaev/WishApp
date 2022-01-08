package ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp.privacypolicy

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.core.webview.WebPageBlock

@ExperimentalMaterialApi
@Composable
fun PrivacyPolicyScreen(
    onBackPressed: () -> Unit,
    viewModel: PrivacyPolicyViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            WishAppTopBar(
                stringResource(R.string.privacy_policy),
                withBackIcon = true,
                onBackPressed = onBackPressed
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        WebPageBlock("https://vitaliybelyaev.github.io/")
    }
}