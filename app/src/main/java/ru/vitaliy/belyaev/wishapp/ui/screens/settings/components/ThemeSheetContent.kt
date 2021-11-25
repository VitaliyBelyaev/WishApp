package ru.vitaliy.belyaev.wishapp.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ThemeSheetContent(
) {
    Column(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)) {
        Text(
            text = "Темная",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(16.dp)
        )
        Text(
            text = "Светлая",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(16.dp)
        )
        Text(
            text = "Как в системе",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(16.dp)
        )
    }
}