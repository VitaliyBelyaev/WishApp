package ru.vitaliy.belyaev.wishapp.ui.screens.main.entity

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jsoup.Jsoup
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.utils.getLinkDescription
import ru.vitaliy.belyaev.wishapp.utils.getLinkImage
import ru.vitaliy.belyaev.wishapp.utils.getLinkTitle
import timber.log.Timber

data class WishItem(
    val wish: Wish,
    val linkInfo: LinkInfo? = null
)

@ExperimentalCoroutinesApi
suspend fun Wish.toWishItem(): WishItem = suspendCancellableCoroutine { continuation ->
    if (link.isBlank()) {
        continuation.resume(WishItem(this), null)
        return@suspendCancellableCoroutine
    }

    try {
        val document = Jsoup.connect(link).get()
        val title = document.getLinkTitle()
        val description = document.getLinkDescription()
        val image = document.getLinkImage()
        val wishItem = if (title.isBlank() && description.isBlank() && image.isBlank()) {
            WishItem(this)
        } else {
            WishItem(this, LinkInfo(title, description, image))
        }
        continuation.resume(wishItem, null)
    } catch (t: Throwable) {
        Timber.e(t)
        continuation.resume(WishItem(this), null)
    }
}
