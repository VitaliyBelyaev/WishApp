package ru.vitaliy.belyaev.wishapp.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.bottombar.WishAppBottomBar
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar

@Composable
fun MainScreen(
    onWishClicked: (Wish) -> Unit,
    onAddWishClicked: () -> Unit,
    onSettingIconClicked: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val fabShape = RoundedCornerShape(50)

    Scaffold(
        topBar = {
            WishAppTopBar(
                title = stringResource(R.string.app_name),
                actions = {
                    IconButton(onClick = { onSettingIconClicked() }) {
                        Icon(
                            Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        bottomBar = { WishAppBottomBar(fabShape) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddWishClicked() },
                shape = fabShape
            ) {
                Icon(Filled.Add, "Add")
            }
        },
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        val scrollState = rememberScrollState()
        val items: List<Wish> by viewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(scrollState)
        ) {

            items.forEachIndexed { index, wish ->
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWishClicked.invoke(wish) }
                        .padding(16.dp),
                    text = wish.title
                )

                if (index != items.lastIndex) {
                    Divider()
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen(
        {},
        {},
        {},
    )
}