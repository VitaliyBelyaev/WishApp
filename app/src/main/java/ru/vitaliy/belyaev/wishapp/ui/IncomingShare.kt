package ru.vitaliy.belyaev.wishapp.ui

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat
import ru.vitaliy.belyaev.wishapp.shared.domain.SharedTextParser
import ru.vitaliy.belyaev.wishapp.shared.domain.SharedWishDraft

/**
 * То, чем поделились из другого приложения: текст, уже разобранный в `shared`, и картинки.
 * Доступ к чужим картинкам временный, поэтому их читают сразу, когда открывается новая хотелка.
 */
data class IncomingShare(
    val text: SharedWishDraft?,
    val imageUris: List<Uri>,
)

/**
 * `ACTION_SEND` с текстом, `ACTION_SEND` и `ACTION_SEND_MULTIPLE` с картинками. Текст рядом с картинками
 * разбирается так же, как без них. `null`, если прислали не это или ничего полезного.
 */
internal fun Intent.toIncomingShare(ownPackage: String): IncomingShare? {
    val mimeType = type.orEmpty()
    val isImages = mimeType.startsWith("image/")
    val isSupported = (action == Intent.ACTION_SEND || action == Intent.ACTION_SEND_MULTIPLE) &&
        (isImages || mimeType == "text/plain")
    if (!isSupported) return null

    val text = SharedTextParser.parse(
        subject = getStringExtra(Intent.EXTRA_SUBJECT),
        text = getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString(),
    ).takeUnless { it.isEmpty }
    val uris = if (isImages) {
        streamUris().filter { it.isForeignContent(ownPackage) }.distinct()
    } else {
        emptyList()
    }
    return IncomingShare(text = text, imageUris = uris).takeUnless { text == null && uris.isEmpty() }
}

/** Картинки лежат в `EXTRA_STREAM`, некоторые приложения кладут их только в `ClipData`. */
private fun Intent.streamUris(): List<Uri> {
    val extra = if (action == Intent.ACTION_SEND_MULTIPLE) {
        IntentCompat.getParcelableArrayListExtra(this, Intent.EXTRA_STREAM, Uri::class.java).orEmpty()
    } else {
        listOfNotNull(IntentCompat.getParcelableExtra(this, Intent.EXTRA_STREAM, Uri::class.java))
    }
    val clip = clipData
    return when {
        extra.isNotEmpty() || clip == null -> extra
        else -> (0 until clip.itemCount).mapNotNull { clip.getItemAt(it).uri }
    }
}

/** Только чужие `content://`: `file://` и свой провайдер открыли бы файлы самого приложения. */
private fun Uri.isForeignContent(ownPackage: String): Boolean =
    scheme == ContentResolver.SCHEME_CONTENT && authority?.startsWith(ownPackage) != true
