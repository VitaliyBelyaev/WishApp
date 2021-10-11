package ru.vitaliy.belyaev.wishapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.vitaliy.belyaev.wishapp.navigation.Navigation
import ru.vitaliy.belyaev.wishapp.ui.App

@Composable
fun Main(navController: NavController? = null) =
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Main") }
            )
        }) {

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = "Это главная"
        )
    }

@Preview
@Composable
fun MainPreview() {
    Main()
}