package ru.vitaliy.belyaev.wishapp.ui.wishdetailed

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.ui.topappbar.TopAppBar

@Composable
fun WishDetailed(
    onBackPressed: () -> Unit,
    wishId: String = ""
) =
    TopAppBar(
        onBackPressed = onBackPressed,
        "WishDetailed",
        withBackIcon = true
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = "Это WishDetailed id=$wishId"
        )
    }

@Preview
@Composable
fun WishDetailedPreview() {
    WishDetailed(
        {}
    )
}