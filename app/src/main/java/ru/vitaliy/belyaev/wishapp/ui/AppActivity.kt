package ru.vitaliy.belyaev.wishapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint
import ru.vitaliy.belyaev.wishapp.navigation.Navigation
import ru.vitaliy.belyaev.wishapp.theme.WishAppTheme

@AndroidEntryPoint
class AppActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App { Navigation() }
        }
    }
}

@Composable
fun App(content: @Composable () -> Unit) {
    WishAppTheme {
        Surface(color = MaterialTheme.colors.background) {
            content()
        }
    }
}