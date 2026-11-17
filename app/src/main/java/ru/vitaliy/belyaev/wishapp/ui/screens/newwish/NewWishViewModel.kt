package ru.vitaliy.belyaev.wishapp.ui.screens.newwish

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.Collator
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.data.image.PickedImageReader
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.NewWish
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.TagsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DispatcherProvider

sealed interface NewWishEvent {
    /** Хотелка записана. [keepOpen]: создана галочкой клавиатуры, панель ждёт следующую. */
    data class Created(val keepOpen: Boolean) : NewWishEvent

    /** Шторку закрыли без галочки, черновик можно вернуть. */
    data object NotCreated : NewWishEvent

    /** Ни одно из выбранных фото не прочиталось. */
    data object ImagesFailed : NewWishEvent
}

/** Всё, что человек делает в шторке и в выборе ярлыков. */
sealed interface NewWishAction {
    data class TitleChanged(val value: String) : NewWishAction
    data object CommentOpened : NewWishAction
    data class CommentChanged(val value: String) : NewWishAction
    data object LinkFieldOpened : NewWishAction
    data class LinkInputChanged(val value: String) : NewWishAction

    /** Enter в поле ссылки: ссылка уходит в чипы, ошибка видна сразу. */
    data object LinkInputDone : NewWishAction

    /** Чип «Вставить ссылку» или кнопка «Вставить»: текст буфера прочитан по нажатию. */
    data class Pasted(val text: String?) : NewWishAction
    data class LinkRemoved(val link: String) : NewWishAction
    data class ImagesPicked(val uris: List<Uri>) : NewWishAction
    data class ImageRemoved(val imageId: String) : NewWishAction
    data class TagToggled(val tagId: String) : NewWishAction
    data class TagCreated(val title: String) : NewWishAction
    data object Cleared : NewWishAction

    /** Кнопка-галочка создаёт и закрывает. Галочка клавиатуры в названии создаёт и ждёт следующую. */
    data class Create(val keepOpen: Boolean) : NewWishAction

    /** Свайп вниз, нажатие мимо или «назад»: хотелка не создаётся, черновик остаётся. */
    data object Dismissed : NewWishAction
}

/**
 * Шторка новой хотелки. Черновик живёт, пока жив список, то есть до перезапуска: закрытая без
 * галочки шторка открывается с ним снова. Хотелка пишется в базу одним разом по галочке, на экране или на клавиатуре.
 */
@HiltViewModel
class NewWishViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val wishesRepository: WishesRepository,
    private val tagsRepository: TagsRepository,
    private val dispatcherProvider: DispatcherProvider,
) : BaseViewModel() {

    private val _draft = MutableStateFlow(NewWishDraft())
    val draft: StateFlow<NewWishDraft> = _draft.asStateFlow()

    private val _isOpen = MutableStateFlow(false)
    val isOpen: StateFlow<Boolean> = _isOpen.asStateFlow()

    /** Все ярлыки по алфавиту с учётом языка, как в шторке ярлыков. */
    val tags: StateFlow<List<TagEntity>> = tagsRepository.observeAllTags()
        .map { tags ->
            val collator = Collator.getInstance()
            tags.sortedWith { a, b -> collator.compare(a.title, b.title) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    private val eventChannel = Channel<NewWishEvent>(Channel.BUFFERED)
    val events: Flow<NewWishEvent> = eventChannel.receiveAsFlow()

    /** Ярлык фильтра, в котором открыта шторка: с него начинается и следующий черновик. */
    private var contextTagId: String? = null
    private var isCreating = false

    /** Плюс в доке, плюс пустого раздела или «Создать» из поиска с запросом в названии. */
    fun open(contextTagId: String?, title: String? = null) {
        this.contextTagId = contextTagId
        _draft.update { it.openedIn(contextTagId, title) }
        _isOpen.value = true
    }

    /** «Вернуться» в снэкбаре «Хотелка не создана». */
    fun onReturnToDraft() {
        open(contextTagId)
    }

    fun onAction(action: NewWishAction) {
        when (action) {
            is NewWishAction.Pasted -> onPasted(action.text)
            is NewWishAction.ImagesPicked -> onImagesPicked(action.uris)
            is NewWishAction.TagCreated -> onTagCreated(action.title)
            NewWishAction.Cleared -> _draft.value = NewWishDraft.fresh(contextTagId)
            is NewWishAction.Create -> onCreate(action.keepOpen)
            NewWishAction.Dismissed -> onDismissed()
            else -> _draft.update { it.edited(action) }
        }
    }

    private fun onPasted(text: String?) {
        if (text.isNullOrBlank()) return
        _draft.update { draft ->
            draft.withPastedLinks(text) ?: draft.copy(linkInput = text.trim(), isLinkInvalid = true)
        }
    }

    private fun onImagesPicked(uris: List<Uri>) {
        val free = MAX_DRAFT_IMAGES - _draft.value.images.size
        if (uris.isEmpty() || free <= 0) return
        launchSafe {
            val images = withContext(dispatcherProvider.io()) {
                // Оригиналы обрабатываем по одному, чтобы не держать их в памяти все сразу
                uris.take(free).mapNotNull { uri ->
                    runCatching { PickedImageReader.read(uri, context.contentResolver) }.getOrNull()
                        ?.let { DraftImage(UUID.randomUUID().toString(), it) }
                }
            }
            if (images.isEmpty()) {
                eventChannel.send(NewWishEvent.ImagesFailed)
            } else {
                _draft.update { it.copy(images = (it.images + images).take(MAX_DRAFT_IMAGES)) }
            }
        }
    }

    /** «Создать «…»»: такой же ярлык второй раз не создаётся, а выбирается. */
    private fun onTagCreated(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        launchSafe {
            val tagId = tagsRepository.insertTag(trimmed)
            _draft.update { draft -> if (tagId in draft.tagIds) draft else draft.copy(tagIds = draft.tagIds + tagId) }
        }
    }

    private fun onDismissed() {
        if (!_isOpen.value) return
        _isOpen.value = false
        if (_draft.value.isClearable) eventChannel.trySend(NewWishEvent.NotCreated)
    }

    private fun onCreate(keepOpen: Boolean) {
        // Ссылка, оставленная в поле, тоже сохраняется. Не ссылка: ошибка, хотелка не создаётся
        val draft = _draft.value.withLinkInputApplied()
        when {
            isCreating -> Unit
            draft == null -> _draft.update { it.copy(isLinkInvalid = true) }
            draft.canCreate -> create(draft, keepOpen)
        }
    }

    private fun create(draft: NewWishDraft, keepOpen: Boolean) {
        isCreating = true
        launchSafe {
            try {
                wishesRepository.createWish(draft.toNewWish())
                _draft.value = NewWishDraft.fresh(contextTagId)
                if (!keepOpen) _isOpen.value = false
                eventChannel.send(NewWishEvent.Created(keepOpen))
            } finally {
                isCreating = false
            }
        }
    }

    private fun NewWishDraft.toNewWish() = NewWish(
        title = title.trim(),
        comment = comment.orEmpty().trim(),
        links = links,
        tagIds = tagIds,
        images = images.map { it.data },
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
