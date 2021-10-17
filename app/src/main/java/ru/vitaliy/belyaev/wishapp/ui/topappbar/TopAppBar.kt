package ru.vitaliy.belyaev.wishapp.ui.topappbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun TopAppBar(
    onBackPressed: (() -> Unit)? = null,
    title: String = "",
    withBackIcon: Boolean = false,
    content: @Composable (TopAppBarData) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            val navIcon: @Composable (() -> Unit)? =
                if (withBackIcon) {
                    {
                        IconButton(onClick = { onBackPressed?.invoke() }) {
                            Icon(Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                } else {
                    null
                }
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = navIcon
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        content(TopAppBarData(snackbarHostState, it))
    }
}

data class TopAppBarData(
    val snackbarHostState: SnackbarHostState,
    val paddingValues: PaddingValues
)