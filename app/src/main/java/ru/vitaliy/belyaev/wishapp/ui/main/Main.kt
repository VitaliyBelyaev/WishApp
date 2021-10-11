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
import androidx.navigation.NavController
import ru.vitaliy.belyaev.wishapp.navigation.WishDetailedScreen
import ru.vitaliy.belyaev.wishapp.ui.topappbar.TopAppBar

@Composable
fun Main(navController: NavController? = null) =
    TopAppBar(title = "Main") { topBarAppData ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .padding(topBarAppData.paddingValues)
                .verticalScroll(scrollState)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = "Это главная"
            )

            Divider()

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController?.navigate(WishDetailedScreen.route("1")) }
                    .padding(16.dp),
                text = "Wish item 1"
            )
        }

    }

@Preview
@Composable
fun MainPreview() {
    Main()
}