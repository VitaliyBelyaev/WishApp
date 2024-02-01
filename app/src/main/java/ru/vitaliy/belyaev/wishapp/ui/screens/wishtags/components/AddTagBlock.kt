package ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon

@Composable
fun AddTagBlock(
    tagName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        ThemedIcon(
            painterResource(R.drawable.ic_add),
            contentDescription = "Add label",
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        Text(
            text = stringResource(R.string.create_new_pattern, tagName),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically)
        )
    }
}

@Composable
@Preview
fun AddTagBlockPreview() {
    AddTagBlock("ДР", {})
}