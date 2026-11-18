package ru.vitaliy.belyaev.wishapp.ui.screens.newwish

import ru.vitaliy.belyaev.wishapp.shared.domain.LinkParser
import ru.vitaliy.belyaev.wishapp.shared.domain.SharedWishDraft

/** Сколько фото можно добавить к хотелке, как в системном выборе фото. */
internal const val MAX_DRAFT_IMAGES = 5

/** Фото черновика: уже уменьшено и лежит в памяти, в базу попадёт вместе с хотелкой. */
class DraftImage(val id: String, val data: ByteArray)

/**
 * Черновик новой хотелки по `QuickAddSheet`. Пока не нажата галочка или Enter, в базе ничего нет.
 *
 * [comment] и [linkInput] равны `null`, пока их поля не открыты чипами.
 */
data class NewWishDraft(
    val title: String = "",
    val comment: String? = null,
    val links: List<String> = emptyList(),
    val linkInput: String? = null,
    val isLinkInvalid: Boolean = false,
    val images: List<DraftImage> = emptyList(),
    val tagIds: List<String> = emptyList(),
    /** Ярлык фильтра, подставленный при открытии: сам по себе он черновик не меняет. */
    val presetTagId: String? = null,
    /** Пришёл из «Поделиться» и открыт впервые: это новая хотелка, «Очистить» ей не нужна. */
    val isShared: Boolean = false,
) {

    /** Достаточно чего-то одного: названия, комментария, ссылки или фото. Пробелы и ярлык не в счёт. */
    val canCreate: Boolean
        get() = title.isNotBlank() ||
            comment.orEmpty().isNotBlank() ||
            links.isNotEmpty() ||
            images.isNotEmpty() ||
            linkInput.orEmpty().isNotBlank()

    /**
     * Человек что-то сделал в шторке, включая выбор ярлыков. Тогда видно «Очистить», а закрытие
     * без галочки предлагает «Вернуться». Подставленный ярлык фильтра не в счёт.
     */
    val isClearable: Boolean
        get() = canCreate || comment != null || linkInput != null || tagIds != listOfNotNull(presetTagId)

    val canAddImages: Boolean get() = images.size < MAX_DRAFT_IMAGES

    /**
     * Поле ссылки сохранено: адреса из него, а если их нет, сам текст встают к остальным ссылкам
     * без дублей, поле закрывается. Проверка не мешает: так по галочке создания.
     */
    fun withLinkInputSaved(): NewWishDraft {
        val saved = LinkParser.linksToSave(linkInput.orEmpty())
        return copy(links = (links + saved).distinct(), linkInput = null, isLinkInvalid = false)
    }

    /**
     * «Готово» на клавиатуре в поле ссылки. Не ссылку сначала подсвечиваем, чтобы человек
     * заметил, повторное «Готово» сохраняет её как есть.
     */
    fun withLinkInputDone(): NewWishDraft {
        val input = linkInput.orEmpty()
        val looksLikeLink = input.isBlank() || LinkParser.parseLinkInput(input).isNotEmpty()
        return if (looksLikeLink || isLinkInvalid) withLinkInputSaved() else copy(isLinkInvalid = true)
    }

    /**
     * Присланное из другого приложения в открытую шторку. Ссылки и фото встают в конец, фото не
     * больше [MAX_DRAFT_IMAGES]. Название и комментарий заполняются, только если они пусты:
     * то, что человек уже написал, не трогаем.
     */
    fun withShared(text: SharedWishDraft?, sharedImages: List<DraftImage>): NewWishDraft {
        val sharedComment = text?.comment.orEmpty()
        return copy(
            title = title.ifBlank { text?.title.orEmpty() },
            comment = if (comment.isNullOrBlank() && sharedComment.isNotBlank()) sharedComment else comment,
            links = (links + text?.links.orEmpty()).distinct(),
            images = (images + sharedImages).take(MAX_DRAFT_IMAGES),
        )
    }

    /** Новые ярлыки встают в конец, первый выбранный идёт на карточку. */
    fun withTagToggled(tagId: String): NewWishDraft =
        copy(tagIds = if (tagId in tagIds) tagIds - tagId else tagIds + tagId)

    /** Текст из буфера или поля ссылки: адреса из него идут в ссылки без дублей. `null`, если адресов нет. */
    fun withPastedLinks(text: String): NewWishDraft? = LinkParser.parseLinkInput(text)
        .takeIf { it.isNotEmpty() }
        ?.let { parsed -> copy(links = (links + parsed).distinct(), linkInput = null, isLinkInvalid = false) }

    /**
     * Шторку открыли: пустой черновик получает ярлык фильтра первым. Черновик с содержимым не
     * перезаписывается, его ярлыки остаются как были.
     */
    fun openedIn(contextTagId: String?, title: String?): NewWishDraft {
        val base = if (isClearable) copy(isShared = false) else fresh(contextTagId)
        return if (title.isNullOrBlank() || base.title.isNotBlank()) base else base.copy(title = title)
    }

    companion object {
        /** Чистый черновик после создания или «Очистить»: в ярлыке он снова начинается с этого ярлыка. */
        fun fresh(contextTagId: String?): NewWishDraft =
            NewWishDraft(tagIds = listOfNotNull(contextTagId), presetTagId = contextTagId)
    }
}

/** Правки, которые меняют только черновик. Остальные действия обрабатывает [NewWishViewModel]. */
internal fun NewWishDraft.edited(action: NewWishAction): NewWishDraft = when (action) {
    is NewWishAction.TitleChanged -> copy(title = action.value)
    NewWishAction.CommentOpened -> copy(comment = comment.orEmpty())
    is NewWishAction.CommentChanged -> copy(comment = action.value)
    NewWishAction.LinkFieldOpened -> copy(linkInput = linkInput.orEmpty())
    is NewWishAction.LinkInputChanged -> copy(linkInput = action.value, isLinkInvalid = false)
    NewWishAction.LinkInputDone -> withLinkInputDone()
    is NewWishAction.LinkRemoved -> copy(links = links - action.link)
    is NewWishAction.ImageRemoved -> copy(images = images.filterNot { it.id == action.imageId })
    is NewWishAction.TagToggled -> withTagToggled(action.tagId)
    else -> this
}
