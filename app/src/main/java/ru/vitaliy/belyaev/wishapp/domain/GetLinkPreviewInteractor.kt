package ru.vitaliy.belyaev.wishapp.domain

import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.Data
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.LinkInfo
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.LinkPreviewState
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.NoData
import ru.vitaliy.belyaev.wishapp.utils.getLinkImage
import ru.vitaliy.belyaev.wishapp.utils.getLinkTitle
import timber.log.Timber

class GetLinkPreviewInteractor @Inject constructor() {

    suspend operator fun invoke(link: String): LinkPreviewState {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(link)
                val document = Jsoup.parse(url, 3000)
                val title = document.getLinkTitle()
                val image = document.getLinkImage()
                val linkPreviewState = if (title.isBlank()) {
                    NoData
                } else {
                    Data(LinkInfo(title, image))
                }
                linkPreviewState
            } catch (t: Throwable) {
                Timber.e(t)
                NoData
            }
        }
    }
}

