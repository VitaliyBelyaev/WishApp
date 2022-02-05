package ru.vitaliy.belyaev.wishapp.domain

import javax.inject.Inject
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import ru.vitaliy.belyaev.wishapp.data.network.await
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.Data
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.LinkInfo
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.LinkPreviewState
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.None
import ru.vitaliy.belyaev.wishapp.utils.getLinkDescription
import ru.vitaliy.belyaev.wishapp.utils.getLinkImage
import ru.vitaliy.belyaev.wishapp.utils.getLinkTitle
import timber.log.Timber

class GetLinkPreviewInteractor @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    suspend operator fun invoke(link: String): LinkPreviewState {
        try {
            val request = Request.Builder()
                .url(link)
                .build()

            val response = okHttpClient.newCall(request).await()
            val body: String = response.body!!.string()
            val document = Jsoup.parse(body)
            val title = document.getLinkTitle()
            val description = document.getLinkDescription()
            val image = document.getLinkImage()
            val linkPreviewState = if (title.isBlank() && description.isBlank() && image.isBlank()) {
                None
            } else {
                Data(LinkInfo(title, description, image))
            }
            return linkPreviewState
        } catch (t: Throwable) {
            Timber.e(t)
            return None
        }
    }
}

