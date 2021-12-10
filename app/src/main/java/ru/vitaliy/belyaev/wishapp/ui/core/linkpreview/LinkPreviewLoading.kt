package ru.vitaliy.belyaev.wishapp.ui.core.linkpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
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
        val cornerRadius = dimensionResource(R.dimen.base_corner_radius)
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
                .padding(start = 10.dp, top = 8.dp, bottom = 8.dp)
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

            // We need this to fill max height of 2 lines for description in shimmer
            val longInvisibleText =
                """
                    linklinklinklinklinklinklinklinklinklinklinklinklinklinklinklinklin
                    klinklinklinklinklinklinklinklinklinklinklinklinklinklinklinklinklink
                    linklinklinklinklinklinklinklinklinklink
                """.trimIndent()
            Text(
                text = longInvisibleText,
                style = MaterialTheme.typography.body2,
                color = colorResource(R.color.shimmerColor),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
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
    LinkPreviewLoading(PaddingValues())
}