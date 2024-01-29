package ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.ShareData
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.ui.theme.AppButtonDefaults

@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Composable
fun ShareBottomSheetContent(
    wishesToShare: List<WishEntity>,
    onShareClick: (ShareData) -> Unit,
    isPdfShareButtonLoading: Boolean,
    modifier: Modifier = Modifier
) {

    val buttonsContentPadding = PaddingValues(
        start = 24.dp,
        end = 24.dp,
        top = 12.dp,
        bottom = 12.dp
    )

    Column(modifier = modifier.padding(start = 12.dp, end = 12.dp)) {
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = stringResource(R.string.share_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(12.dp))

        FilledTonalButton(
            onClick = { onShareClick(ShareData.Text(wishesToShare)) },
            enabled = !isPdfShareButtonLoading,
            contentPadding = buttonsContentPadding,
            shape = AppButtonDefaults.defaultButtonShape(),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.share_type_text_button_text),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        FilledTonalButton(
            onClick = { onShareClick(ShareData.Pdf(wishesToShare)) },
            enabled = !isPdfShareButtonLoading,
            contentPadding = buttonsContentPadding,
            shape = AppButtonDefaults.defaultButtonShape(),
            modifier = Modifier
                .fillMaxWidth()
        ) {

            if (isPdfShareButtonLoading) {
                Text(
                    text = stringResource(R.string.share_generating_pdf_button_text),
                    style = MaterialTheme.typography.bodyLarge,
                )

                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(22.dp),
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.share_type_pdf_button_text),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalCoroutinesApi::class
)
@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 300
)
@Composable
fun ShareBottomSheetContentPreview() {
    ShareBottomSheetContent(
        wishesToShare = emptyList(),
        {},
        isPdfShareButtonLoading = true
    )
}