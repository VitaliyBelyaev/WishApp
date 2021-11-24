package ru.vitaliy.belyaev.wishapp.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R

@Composable
fun SettingBlock(title: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onClick() }
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
                .fillMaxWidth()
                .weight(8f)
        )
        Icon(
            painterResource(R.drawable.ic_arrow_forward_ios),
            contentDescription = "Arrow",
            modifier = Modifier
                .size(16.dp)
                .weight(1f)
        )
    }
}

@Composable
@Preview
fun SettingBlockPreview() {
    SettingBlock("Тема", {})
}