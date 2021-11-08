package ru.vitaliy.belyaev.wishapp.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.vitaliy.belyaev.model.database.Wish

@Composable
fun WishItem(
    wish: Wish,
    onWishClicked: (Wish) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier.clickable { onWishClicked(wish) }
    ) {
        val (title, link, comment) = createRefs()
        val baseMargin = 16.dp

        Text(
            text = wish.title,
            modifier = Modifier
                .constrainAs(title) {
                    top.linkTo(parent.top, margin = baseMargin)
                    start.linkTo(parent.start, margin = baseMargin)
                    end.linkTo(parent.end, margin = baseMargin)
                }
        )

        Text(
            text = wish.link,
            modifier = Modifier
                .constrainAs(link) {
                    width = Dimension.fillToConstraints
                    top.linkTo(title.bottom, margin = 8.dp)
                    start.linkTo(parent.start, margin = baseMargin)
                    end.linkTo(parent.end, margin = baseMargin)
                }
        )

        Text(
            text = wish.comment,
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(comment) {
                    width = Dimension.fillToConstraints
                    top.linkTo(link.bottom, margin = 8.dp)
                    start.linkTo(parent.start, margin = baseMargin)
                    end.linkTo(parent.end, margin = baseMargin)
                    bottom.linkTo(parent.bottom, margin = baseMargin)
                }
        )
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