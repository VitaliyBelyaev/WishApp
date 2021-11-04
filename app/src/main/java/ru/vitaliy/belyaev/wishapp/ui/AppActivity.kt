package ru.vitaliy.belyaev.wishapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint
import ru.vitaliy.belyaev.wishapp.navigation.Navigation
import ru.vitaliy.belyaev.wishapp.theme.WishAppTheme
import ru.vitaliy.belyaev.wishapp.ui.screens.main.MainViewModel

@AndroidEntryPoint
class AppActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App { Navigation(mainViewModel) }
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