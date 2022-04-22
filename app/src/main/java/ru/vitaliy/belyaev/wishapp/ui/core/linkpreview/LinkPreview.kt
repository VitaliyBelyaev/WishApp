package ru.vitaliy.belyaev.wishapp.ui.core.linkpreview

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.LinkInfo

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun LinkPreview(
    linkInfo: LinkInfo,
    url: String,
    paddingValues: PaddingValues = PaddingValues(),
    onLinkPreviewClick: (String) -> Unit,
) {

    val shape = RoundedCornerShape(dimensionResource(R.dimen.base_corner_radius))
    val borderWidth = 1.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(paddingValues)
            .border(
                width = borderWidth,
                color = MaterialTheme.colors.primary,
                shape = shape
            )
            .clip(shape)
            .clickable { onLinkPreviewClick(url) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imageUrl = linkInfo.imageUrl

        if (imageUrl.isNotBlank()) {
            Image(
                painter = rememberImagePainter(
                    data = linkInfo.imageUrl,
                    builder = {
                        placeholder(R.drawable.ic_image_24)
                        error(R.drawable.ic_image_24)
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
            )
        } else {
            ThemedIcon(
                painter = painterResource(R.drawable.ic_open_link),
                contentDescription = "Open link",
                modifier = Modifier
                    .padding(start = 12.dp)
            )
        }

        Text(
            text = linkInfo.title,
            style = MaterialTheme.typography.body1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp)
        )
    }
}

val LINK_IMAGE_SIZE = 58.dp