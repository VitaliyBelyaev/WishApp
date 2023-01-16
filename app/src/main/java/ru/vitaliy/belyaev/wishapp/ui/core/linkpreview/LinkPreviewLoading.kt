package ru.vitaliy.belyaev.wishapp.ui.core.linkpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import ru.vitaliy.belyaev.wishapp.R

@Composable
fun LinkPreviewLoading(paddingValues: PaddingValues) {

    val shape = RoundedCornerShape(dimensionResource(R.dimen.base_corner_radius))
    val borderWidth = 1.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(paddingValues)
            .border(
                width = borderWidth,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                shape = shape
            )
            .clip(shape)
            .shimmer()
            .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    )
}

@Preview
@Composable
fun LinkPreviewLoadingPreview() {
    LinkPreviewLoading(PaddingValues())
}