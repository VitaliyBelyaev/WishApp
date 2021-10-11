package ru.vitaliy.belyaev.wishapp.ui.wishdetailed

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun WishDetailed(navController: NavController? = null, wishId: String = "") =
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "WishDetailed") }
            )
        }) {

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = "Это WishDetailed"
        )
    }

@Preview
@Composable
fun WishDetailedPreview() {
    WishDetailed()
}