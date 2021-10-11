package ru.vitaliy.belyaev.wishapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.vitaliy.belyaev.wishapp.navigation.Navigation
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppTheme

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