package ru.vitaliy.belyaev.wishapp.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.LinkPreview
import ru.vitaliy.belyaev.wishapp.ui.core.linkpreview.LinkPreviewLoading
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.Data
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.Loading
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.None
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishItem

@Composable
fun WishItemBlock(
    wishItem: WishItem,
    onWishClicked: (Wish) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWishClicked(wishItem.wish) }
            .padding(16.dp)
    ) {

        val wish = wishItem.wish
        val (title, titleColor) = if (wish.title.isNotBlank()) {
            wish.title to Color.Unspecified
        } else {
            stringResource(R.string.without_title) to Color.Gray
        }
        Text(
            text = title,
            color = titleColor,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.fillMaxWidth()
        )
        if (wish.comment.isNotBlank()) {
            Text(
                text = wish.comment,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        when (val linkPreviewState = wishItem.linkPreviewState) {
            is Data -> {
                val paddingValues = PaddingValues(top = 8.dp)
                LinkPreview(paddingValues, linkPreviewState.linkInfo)
            }
            is Loading -> {
                val paddingValues = PaddingValues(top = 8.dp)
                LinkPreviewLoading(paddingValues)
            }
            is None -> {
                //nothing
            }
        }
    }
}

//@Preview
//@Composable
//fun WishItemPreview() {
//    WishItem(
//        Wish(
//            "1",
//            "Шуруповерт",
//            "https://www.citilink.ru/product/drel-shurupovert-makita-df333dwye-1-5ach-s-dvumya-akkumulyatorami-1149175/?region_id=123062&gclid=Cj0KCQiAsqOMBhDFARIsAFBTN3fKH6UIMpyxbwdFyK2JV0Z0TQUUZSysOfLWsfDVYYfRxFSCFTupH9saArdgEALw_wcB",
//            "Не китай и чтобы в наборе были головки",
//            false,
//            0,
//            0,
//            emptyList()
//        ),
//        {}
//    )
//}