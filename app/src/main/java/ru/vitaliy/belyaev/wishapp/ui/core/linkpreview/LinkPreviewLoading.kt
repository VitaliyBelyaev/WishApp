package ru.vitaliy.belyaev.wishapp.ui.core.linkpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import ru.vitaliy.belyaev.wishapp.R

@Composable
fun LinkPreviewLoading(paddingValues: PaddingValues) =
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shimmer()
            .padding(paddingValues),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val cornerRadius = 4.dp
        Box(
            modifier = Modifier
                .size(LINK_IMAGE_SIZE)
                .background(
                    color = colorResource(R.color.shimmerColor),
                    shape = RoundedCornerShape(cornerRadius)
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
        ) {
            Text(
                text = "",
                style = MaterialTheme.typography.body1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colorResource(R.color.shimmerColor),
                        shape = RoundedCornerShape(cornerRadius)
                    )
            )

            Text(
                text = "",
                style = MaterialTheme.typography.body2,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .background(
                        color = colorResource(R.color.shimmerColor),
                        shape = RoundedCornerShape(cornerRadius)
                    )
            )
        }
    }

@Preview
@Composable
fun LinkPreviewLoadingPreview() {
    LinkPreviewLoading(PaddingValues(0.dp))
}