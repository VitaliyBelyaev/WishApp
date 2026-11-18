package ru.vitaliy.belyaev.wishapp.ui.screens.wish

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import ru.vitaliy.belyaev.wishapp.shared.domain.LinkParser
import ru.vitaliy.belyaev.wishapp.shared.domain.LinksAdapter
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.TagsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishTagRelationRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.WishListSharer

/** Хотелка из базы и все ярлыки для выбора. `null`, пока не загрузилось. */
data class WishScreenState(val wish: WishEntity, val allTags: List<TagEntity>)

/** Разовые события экрана хотелки. */
sealed interface WishScreenEvent {
    data object PhotoFailed : WishScreenEvent

    /** Окно «Поделиться» собрано, его запускает экран. */
    data class Share(val intent: Intent) : WishScreenEvent
}

/**
 * Экран хотелки нового вида: правка на месте, всё сохраняется само.
 *
 * Название и комментарий живут здесь, база получает каждую правку по очереди. Из базы текст берётся
 * только при первой загрузке: иначе ответ базы на прошлую букву возвращал бы курсор назад.
 */
@Suppress("TooManyFunctions")
@HiltViewModel(assistedFactory = WishViewModel.Factory::class)
class WishViewModel @AssistedInject constructor(
    @Assisted private val wishId: String,
    private val wishesRepository: WishesRepository,
    private val tagsRepository: TagsRepository,
    private val wishTagRelationRepository: WishTagRelationRepository,
    private val writeQueue: WishWriteQueue,
    private val sharer: WishListSharer,
) : BaseViewModel(), WishEditor, WishTagsEditor {

    @AssistedFactory
    interface Factory {
        fun create(wishId: String): WishViewModel
    }

    private val _state = MutableStateFlow<WishScreenState?>(null)
    val state: StateFlow<WishScreenState?> = _state.asStateFlow()

    private val _title = MutableStateFlow<String?>(null)
    val title: StateFlow<String?> = _title.asStateFlow()

    private val _comment = MutableStateFlow<String?>(null)
    val comment: StateFlow<String?> = _comment.asStateFlow()

    private val _linkInput = MutableStateFlow("")
    val linkInput: StateFlow<String> = _linkInput.asStateFlow()

    private val _isLinkInvalid = MutableStateFlow(false)
    val isLinkInvalid: StateFlow<Boolean> = _isLinkInvalid.asStateFlow()

    private val eventChannel = Channel<WishScreenEvent>(Channel.BUFFERED)
    val events: Flow<WishScreenEvent> = eventChannel.receiveAsFlow()

    init {
        launchSafe {
            // Фото, удалённые из просмотра, не видны, пока у снэкбара можно нажать «Вернуть»
            combine(
                wishesRepository.observeWishById(wishId),
                tagsRepository.observeAllTags(),
                writeQueue.hiddenImages,
            ) { wish, tags, hidden ->
                val shown = if (hidden.isEmpty()) {
                    wish
                } else {
                    wish.copy(
                        images = wish.images.filterNot {
                            it.id in hidden
                        },
                    )
                }
                WishScreenState(shown, tags)
            }.collect { loaded ->
                if (_title.value == null) _title.value = loaded.wish.title
                if (_comment.value == null) _comment.value = loaded.wish.comment
                _state.value = loaded
            }
        }
    }

    /**
     * «Поделиться» из «⋮»: с фото уходит картинка и текст подписью. Название и комментарий берутся
     * из полей, а не из базы: последняя правка могла ещё не записаться.
     */
    fun onShareClicked() {
        val wish = _state.value?.wish ?: return
        val current = wish.copy(title = _title.value ?: wish.title, comment = _comment.value ?: wish.comment)
        launchSafe { eventChannel.send(WishScreenEvent.Share(sharer.wishIntent(current))) }
    }

    /** Записи по очереди и вне ViewModel: экран закрывается, не дожидаясь их, см. [WishWriteQueue]. */
    private fun launchWrite(block: suspend () -> Unit) = writeQueue.write(wishId, block)

    private fun setLinkInput(value: String) {
        _linkInput.value = value
        writeQueue.setPendingLink(wishId, value)
    }

    override fun onTitleChange(value: String) {
        _title.value = value
        launchWrite { wishesRepository.updateWishTitle(value, wishId) }
    }

    override fun onCommentChange(value: String) {
        _comment.value = value
        launchWrite { wishesRepository.updateWishComment(value, wishId) }
    }

    override fun onLinkInputChange(value: String) {
        setLinkInput(value)
        _isLinkInvalid.value = false
    }

    /**
     * Enter в поле ссылки: из текста берутся адреса. Не ссылку сначала подсвечиваем, повторный
     * Enter сохраняет её как есть.
     */
    override fun onLinkSubmit() {
        val input = _linkInput.value.trim()
        if (input.isEmpty()) return
        if (LinkParser.parseLinkInput(input).isEmpty() && !_isLinkInvalid.value) {
            _isLinkInvalid.value = true
            return
        }
        setLinkInput("")
        _isLinkInvalid.value = false
        addLinks(LinkParser.linksToSave(input))
    }

    /** «Вставить»: ссылка из буфера сохраняется сразу, как при вводе и Enter. */
    override fun onLinkPasted(text: String?) {
        if (text.isNullOrBlank()) return
        val links = LinkParser.parseLinkInput(text)
        if (links.isEmpty()) {
            setLinkInput(text.trim())
            _isLinkInvalid.value = true
        } else {
            setLinkInput("")
            _isLinkInvalid.value = false
            addLinks(links)
        }
    }

    /**
     * Поле ссылки потеряло фокус: ссылку сохраняем, не ссылку оставляем в поле с подсветкой.
     * Она сохранится как есть при уходе с экрана, см. [WishWriteQueue.finish].
     */
    override fun onLinkFieldLeft() {
        val input = _linkInput.value.trim()
        if (input.isEmpty()) return
        val links = LinkParser.parseLinkInput(input)
        if (links.isEmpty()) {
            _isLinkInvalid.value = true
        } else {
            setLinkInput("")
            addLinks(links)
        }
    }

    private fun addLinks(links: List<String>) = writeQueue.addLinks(wishId, links)

    /** Убирает ссылку. Вернуть: ссылки целиком, как были, с прежним порядком. */
    override fun onLinkRemoved(link: String): () -> Unit {
        val before = _state.value?.wish?.links ?: return {}
        launchWrite {
            val current = wishesRepository.getWishByIdOrNull(wishId)?.links ?: return@launchWrite
            wishesRepository.updateWishLink(LinksAdapter.removeLinkAndGetAccumulatedString(link, current), wishId)
        }
        val restored = LinksAdapter.addLinksAndGetAccumulatedString(before, emptyList())
        return { launchWrite { wishesRepository.updateWishLink(restored, wishId) } }
    }

    /** «Изменить»: ссылка уходит из списка в поле, сохранится снова по Enter или при уходе с поля. */
    override fun onLinkEdit(link: String) {
        launchWrite {
            val current = wishesRepository.getWishByIdOrNull(wishId)?.links ?: return@launchWrite
            wishesRepository.updateWishLink(LinksAdapter.removeLinkAndGetAccumulatedString(link, current), wishId)
        }
        setLinkInput(link)
        _isLinkInvalid.value = false
    }

    override fun onImagesPicked(uris: List<Uri>, contentResolver: ContentResolver) {
        if (uris.isEmpty()) return
        writeQueue.addImages(wishId, uris, contentResolver) { eventChannel.trySend(WishScreenEvent.PhotoFailed) }
    }

    /** Ярлык в «Ярлыках хотелки»: снять или добавить. */
    override fun onTagToggled(tagId: String) {
        val wish = _state.value?.wish ?: return
        launchWrite {
            if (wish.tags.any { it.id == tagId }) {
                wishTagRelationRepository.deleteWishTagRelation(wishId, tagId)
            } else {
                wishTagRelationRepository.insertWishTagRelation(wishId, tagId)
            }
        }
    }

    /** «Создать «…»»: такой же ярлык второй раз не создаётся, а выбирается. */
    override fun onTagCreated(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        launchWrite {
            val tagId = tagsRepository.insertTag(trimmed)
            val current = wishesRepository.getWishByIdOrNull(wishId)?.tags.orEmpty()
            if (current.none { it.id == tagId }) wishTagRelationRepository.insertWishTagRelation(wishId, tagId)
        }
    }

    override fun onTagShown(tagId: String) {
        launchWrite { wishTagRelationRepository.setShownTag(wishId, tagId) }
    }

    /** Убирает ярлык с хотелки. Вернуть: ярлык снова на ней и, если был на карточке, снова там. */
    override fun onTagRemoved(tagId: String): () -> Unit {
        val wasShown = _state.value?.wish?.shownTag()?.id == tagId
        launchWrite { wishTagRelationRepository.deleteWishTagRelation(wishId, tagId) }
        return {
            launchWrite {
                wishTagRelationRepository.insertWishTagRelation(wishId, tagId)
                if (wasShown) wishTagRelationRepository.setShownTag(wishId, tagId)
            }
        }
    }
}

/** Ярлык на карточке: выбранный в «Показывать на карточке», иначе первый добавленный. */
internal fun WishEntity.shownTag(): TagEntity? = tags.firstOrNull { it.id == shownTagId } ?: tags.firstOrNull()
