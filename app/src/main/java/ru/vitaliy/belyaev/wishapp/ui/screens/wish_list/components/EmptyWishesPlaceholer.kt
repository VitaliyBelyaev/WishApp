package ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R

@Composable
fun EmptyWishesPlaceholder(
    text: String,
    onGoToBackupScreenClicked: () -> Unit,
    modifier: Modifier = Modifier,
    showBackupSection: Boolean = false,
) {

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(start = 32.dp, end = 32.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        if (showBackupSection) {
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.backup_wishlist_empty_text),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            TextButton(
                onClick = onGoToBackupScreenClicked,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(text = stringResource(R.string.backup_wishlist_go_to_backup_button_text))
            }
        }
    }
}
