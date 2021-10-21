package ru.vitaliy.belyaev.wishapp.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.ui.topappbar.TopAppBar

@Composable
fun Main(
    onWishClicked: (Wish) -> Unit,
    onAddWishClicked: () -> Unit,
    uiState: StateFlow<List<Wish>>
) =
    TopAppBar(title = "Main") { topBarAppData ->
        val scrollState = rememberScrollState()

        val items: List<Wish> by uiState.collectAsState()

        Column(
            modifier = Modifier
                .padding(topBarAppData.paddingValues)
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

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Cyan)
                    .clickable { onAddWishClicked.invoke() }
                    .padding(16.dp),
                text = "Добавить хотелку"
            )
        }

    }

@Preview
@Composable
fun MainPreview() {
    Main(
        {},
        { },
        MutableStateFlow(emptyList())
    )
}