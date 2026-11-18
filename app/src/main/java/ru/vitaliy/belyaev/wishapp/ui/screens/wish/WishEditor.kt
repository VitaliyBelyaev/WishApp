package ru.vitaliy.belyaev.wishapp.ui.screens.wish

import android.content.ContentResolver
import android.net.Uri

/**
 * Правки хотелки с экрана, без самой ViewModel в параметрах экрана. Убрать ярлык и ссылку можно
 * отменить: возвращённая функция возвращает как было.
 */
interface WishEditor {
    fun onTitleChange(value: String)
    fun onCommentChange(value: String)
    fun onLinkInputChange(value: String)
    fun onLinkSubmit()
    fun onLinkPasted(text: String?)
    fun onLinkFieldLeft()
    fun onLinkEdit(link: String)
    fun onLinkRemoved(link: String): () -> Unit
    fun onImagesPicked(uris: List<Uri>, contentResolver: ContentResolver)
}

/** Ярлыки хотелки: выбор, новый ярлык, показ на карточке и снятие с отменой. */
interface WishTagsEditor {
    fun onTagToggled(tagId: String)
    fun onTagCreated(title: String)
    fun onTagShown(tagId: String)
    fun onTagRemoved(tagId: String): () -> Unit
}
