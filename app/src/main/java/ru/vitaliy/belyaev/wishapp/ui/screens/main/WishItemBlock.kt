package ru.vitaliy.belyaev.wishapp.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishItem
import timber.log.Timber

@Composable
fun WishItemBlock(
    wishItem: WishItem,
    isSelected: Boolean,
    onWishClicked: (Wish) -> Unit,
    onWishLongPress: (Wish) -> Unit,
) {

    val interactionSource = remember { MutableInteractionSource() }

    val backgroundColor: Color = if (isSelected) {
        colorResource(R.color.wishSelectedColor)
    } else {
        Color.Transparent
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .indication(interactionSource, rememberRipple())
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->

                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)

                        tryAwaitRelease()

                        interactionSource.emit(PressInteraction.Release(press))
                    },
                    onTap = {
                        Timber
                            .tag("RTRT")
                            .d("onTap")
                        onWishClicked(wishItem.wish)
                    },
                    onLongPress = {
                        Timber
                            .tag("RTRT")
                            .d("onLongPress")
                        onWishLongPress(wishItem.wish)

                    }
                )
            }
            .padding(16.dp)
    ) {

        val wish = wishItem.wish
        val (title, titleColor) = if (wish.title.isNotBlank()) {
            wish.title to Color.Unspecified
        } else {
            stringResource(R.string.without_title) to Color.Gray
        }
        ConstraintLayout {
            val (titleRef, linkIconRef) = createRefs()
            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.constrainAs(titleRef) {
                    width = Dimension.preferredWrapContent
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    val marginEnd = if (wish.link.isNotBlank()) {
                        36.dp
                    } else {
                        0.dp
                    }
                    end.linkTo(parent.end, margin = marginEnd)
                }
            )
            if (wish.link.isNotBlank()) {
                Icon(
                    painterResource(R.drawable.ic_link),
                    contentDescription = "Link",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(20.dp)
                        .constrainAs(linkIconRef) {
                            start.linkTo(titleRef.end, margin = 8.dp)
                            end.linkTo(parent.end)
                            top.linkTo(titleRef.top)
                            bottom.linkTo(titleRef.bottom)
                        }
                )
            }
        }
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