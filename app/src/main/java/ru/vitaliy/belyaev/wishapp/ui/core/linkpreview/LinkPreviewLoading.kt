package ru.vitaliy.belyaev.wishapp.ui.core.linkpreview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer

@Composable
fun LinkPreviewLoading(paddingValues: PaddingValues) =
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(paddingValues)
            .shimmer(),
        verticalAlignment = Alignment.CenterVertically
    ) {
//        Image(
//            painter = rememberImagePainter(
//                data = linkInfo.image,
//                builder = {
//                    placeholder(R.drawable.ic_image_24)
//                    error(R.drawable.ic_image_24)
//                }
//            ),
//            contentDescription = null,
//            modifier = Modifier
//                .size(58.dp)
//                .padding(4.dp)
//        )
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(start = 8.dp)
//        ) {
//            Text(
//                text = linkInfo.title,
//                style = MaterialTheme.typography.body1,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis,
//                modifier = Modifier.fillMaxWidth(),
//            )
//
//            Text(
//                text = linkInfo.description,
//                style = MaterialTheme.typography.body2,
//                color = Color.Gray,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 4.dp)
//            )
//        }
    }