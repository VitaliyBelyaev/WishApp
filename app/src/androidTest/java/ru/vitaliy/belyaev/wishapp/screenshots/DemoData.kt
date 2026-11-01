package ru.vitaliy.belyaev.wishapp.screenshots

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import java.io.ByteArrayOutputStream
import java.util.UUID
import ru.vitaliy.belyaev.wishapp.shared.data.repository.DatabaseRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.LinksAdapter
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.createEmptyWish

/** Демо-данные для скриншотов: тексты на русском для `ru`, иначе на английском. */
internal class DemoData(language: String) {

    private val isRu = language == "ru"

    private val electronicsTag = text("Техника", "Electronics")
    private val booksTag = text("Книги", "Books")
    private val homeTag = text("Дом", "Home")
    private val travelTag = text("Путешествия", "Travel")

    val firstTagTitle: String = electronicsTag
    val newTagTitle: String = text("Подарки", "Gifts")
    val firstWishTitle: String = text("Беспроводные наушники", "Wireless headphones")
    val firstWishComment: String = text(
        "С шумоподавлением, лучше чёрные. Хотелось бы к отпуску.",
        "With noise cancelling, preferably black. Would love to have them by the vacation."
    )

    /** Порядок — как на экране: сверху то, что добавлено последним. */
    private val activeWishes = listOf(
        DemoWish(
            title = firstWishTitle,
            comment = firstWishComment,
            links = listOf("https://example.com/headphones", "https://example.org/headphones-review"),
            tags = listOf(electronicsTag),
            imageColors = listOf(
                Color.parseColor("#3F51B5") to Color.parseColor("#00BCD4"),
                Color.parseColor("#E91E63") to Color.parseColor("#FF9800"),
                Color.parseColor("#009688") to Color.parseColor("#CDDC39"),
            ),
        ),
        DemoWish(
            title = text("Кофемашина", "Coffee machine"),
            comment = text("Рожковая, с капучинатором", "Espresso machine with a milk frother"),
            links = listOf("https://example.com/coffee-machine"),
            tags = listOf(homeTag, electronicsTag),
        ),
        DemoWish(
            title = text("«Мастер и Маргарита»", "The Master and Margarita"),
            comment = text("Подарочное издание с иллюстрациями", "Illustrated gift edition"),
            tags = listOf(booksTag),
        ),
        DemoWish(
            title = text("Поездка в Грузию", "Trip to Georgia"),
            comment = text("Тбилиси и Казбеги, лучше в сентябре", "Tbilisi and Kazbegi, ideally in September"),
            tags = listOf(travelTag),
        ),
        DemoWish(
            title = text("Настольная лампа", "Desk lamp"),
            tags = listOf(homeTag),
        ),
        DemoWish(
            title = text("Рюкзак для ноутбука", "Laptop backpack"),
            links = listOf("https://example.com/backpack"),
            tags = listOf(travelTag),
        ),
        DemoWish(
            title = text("Электронная книга", "E-reader"),
            comment = text("Экран от 7 дюймов", "7-inch screen or larger"),
            tags = listOf(electronicsTag, booksTag),
        ),
    )

    private val completedWishes = listOf(
        DemoWish(
            title = text("Умная колонка", "Smart speaker"),
            tags = listOf(electronicsTag),
        ),
        DemoWish(
            title = text("Плед", "Blanket"),
            comment = text("Шерстяной, серый", "Grey wool"),
            tags = listOf(homeTag),
        ),
    )

    suspend fun seed(repository: DatabaseRepository) {
        repository.clearAllWishes()
        repository.clearAllTags()

        val tagIds = listOf(electronicsTag, booksTag, homeTag, travelTag)
            .associateWith { repository.insertTag(it) }

        activeWishes.reversed().forEach { insertWish(repository, it, tagIds, isCompleted = false) }
        completedWishes.reversed().forEach { insertWish(repository, it, tagIds, isCompleted = true) }
    }

    private suspend fun insertWish(
        repository: DatabaseRepository,
        demoWish: DemoWish,
        tagIds: Map<String, String>,
        isCompleted: Boolean,
    ) {
        val linkString = demoWish.links.fold("") { accumulated, link ->
            LinksAdapter.addLinkAndGetAccumulatedString(link, LinksAdapter.getLinksListFromString(accumulated))
        }
        val wish = createEmptyWish().copy(
            title = demoWish.title,
            comment = demoWish.comment,
            link = linkString,
        )
        repository.insertWish(wish)
        demoWish.tags.forEach { repository.insertWishTagRelation(wish.id, tagIds.getValue(it)) }
        demoWish.imageColors.forEach { (startColor, endColor) ->
            repository.insertImage(
                ImageEntity(
                    id = UUID.randomUUID().toString(),
                    wishId = wish.id,
                    rawData = createImage(startColor, endColor),
                )
            )
        }
        if (isCompleted) {
            repository.updateWishIsCompleted(true, wish.id)
        }
    }

    private fun createImage(startColor: Int, endColor: Int): ByteArray {
        val width = 1280
        val height = 960
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            startColor,
            endColor,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
        paint.color = Color.WHITE
        paint.alpha = 70
        canvas.drawCircle(width * 0.3f, height * 0.4f, height * 0.28f, paint)
        canvas.drawCircle(width * 0.7f, height * 0.65f, height * 0.18f, paint)

        return ByteArrayOutputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it)
            it.toByteArray()
        }
    }

    private fun text(ru: String, en: String): String = if (isRu) ru else en

    private class DemoWish(
        val title: String,
        val comment: String = "",
        val links: List<String> = emptyList(),
        val tags: List<String> = emptyList(),
        val imageColors: List<Pair<Int, Int>> = emptyList(),
    )
}
