package ru.vitaliy.belyaev.wishapp.domain

import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.model.network.await
import ru.vitaliy.belyaev.wishapp.model.repository.DatabaseRepository
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.Data
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.LinkInfo
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.None
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishItem
import ru.vitaliy.belyaev.wishapp.utils.getLinkDescription
import ru.vitaliy.belyaev.wishapp.utils.getLinkImage
import ru.vitaliy.belyaev.wishapp.utils.getLinkTitle
import timber.log.Timber

class WishInteractor @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val okHttpClient: OkHttpClient
) {

    suspend fun getWishItems(): Flow<List<WishItem>> {
        return databaseRepository
            .getAll()
            .map {
                val wishItems = mutableListOf<WishItem>()
                for (wish in it) {
                    wishItems.add(wish.toWishItem())
                }
                wishItems
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun getWishItem(id: String): Flow<WishItem> {
        return databaseRepository
            .getById(id)
            .map { it.toWishItem() }
            .flowOn(Dispatchers.IO)
    }

    @ExperimentalCoroutinesApi
    private suspend fun Wish.toWishItem(): WishItem {
        if (link.isBlank()) {
            return WishItem(this, None)
        }

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
            val wishItem = if (title.isBlank() && description.isBlank() && image.isBlank()) {
                WishItem(this, None)
            } else {
                WishItem(this, Data(LinkInfo(title, description, image)))
            }
            return wishItem
        } catch (t: Throwable) {
            Timber.e(t)
            return WishItem(this, None)
        }
    }
}

