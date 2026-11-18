package ru.vitaliy.belyaev.wishapp.ui.screens.photos

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

/**
 * Связь фото на экране хотелки с просмотром, как в Telegram. Хотелка сообщает, где на экране её фото,
 * а просмотр — какое фото сейчас открыто. По этим данным просмотр выращивает фото из его места и
 * сжимает обратно, а хотелка листает своё фото к последнему просмотренному.
 *
 * Пока просмотр открыт, хотелка прячет у себя тень и кнопки над фото, а когда просмотр открылся
 * целиком, и само открытое фото: иначе при свайпе видно два одинаковых. Во время входа фото на месте:
 * растущее фото отъезжает от краёв, и под ним должно темнеть фото, а не мелькать фон шапки.
 */
@Stable
class PhotoHeroLink {

    /** Хотелка, чьё фото сейчас на экране. */
    private var wishId by mutableStateOf<String?>(null)
    private var hero by mutableStateOf<HeroRects?>(null)

    /** Фото, открытое в просмотре. `null`, пока просмотр закрыт. */
    var viewedImageId by mutableStateOf<String?>(null)

    /** Просмотр открыт или открывается: тень и кнопки над фото хотелки спрятаны. */
    var viewerOpen by mutableStateOf(false)

    /** Просмотр открылся целиком: фото хотелки под ним спрятано. */
    var heroPhotoHidden by mutableStateOf(false)

    /** Где фото хотелки: [bounds] в координатах корня, [sheetTop] — край шторки под ним. */
    fun onHeroPositioned(wishId: String, bounds: Rect, sheetTop: Float, sheetCorner: Float) {
        this.wishId = wishId
        hero = HeroRects(bounds = bounds, sheetTop = sheetTop, sheetCorner = sheetCorner)
    }

    fun onHeroGone(wishId: String) {
        if (this.wishId == wishId) {
            this.wishId = null
            hero = null
        }
    }

    /** Место фото хотелки [wishId] относительно точки [origin]; `null`, если его нет на экране. */
    internal fun heroOf(wishId: String, origin: Offset): HeroRects? =
        if (this.wishId == wishId) hero?.translate(-origin) else null

    /** Фото, которое хотелка [wishId] прячет, пока его показывает просмотр. */
    fun hiddenImageOf(wishId: String): String? = if (heroPhotoHidden && this.wishId == wishId) viewedImageId else null

    /** Просмотр поверх хотелки [wishId]: её тень и кнопки над фото спрятаны. */
    fun isViewerOver(wishId: String): Boolean = viewerOpen && this.wishId == wishId

    /** Какое фото листать хотелке [wishId]: открытое в просмотре. */
    fun viewedImageOf(wishId: String): String? = if (this.wishId == wishId) viewedImageId else null
}

/** Фото хотелки на экране: [bounds] — вся его область, ниже [sheetTop] его закрывает шторка со скруглением. */
internal class HeroRects(val bounds: Rect, val sheetTop: Float, val sheetCorner: Float) {
    fun translate(offset: Offset) = HeroRects(bounds.translate(offset), sheetTop + offset.y, sheetCorner)
}

/** Ключ кэша фото хотелки: по нему переход берёт уже загруженную картинку, без ожидания. */
internal fun heroCacheKey(imageId: String) = "wish_photo_$imageId"
