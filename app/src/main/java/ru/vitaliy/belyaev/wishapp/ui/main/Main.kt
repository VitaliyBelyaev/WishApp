package ru.vitaliy.belyaev.wishapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.entity.Wish
import ru.vitaliy.belyaev.wishapp.ui.topappbar.TopAppBar

@Composable
fun Main(
    onWishClicked: (Wish) -> Unit,
    items: List<Wish>
) =
    TopAppBar(title = "Main") { topBarAppData ->
        val scrollState = rememberScrollState()

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
                    text = wish.value
                )

                if (index != items.lastIndex) {
                    Divider()
                }
            }
        }

    }

@Preview
@Composable
fun MainPreview() {
    Main(
        {},
        emptyList()
    )
}