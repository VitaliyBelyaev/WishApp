package ru.vitaliy.belyaev.wishapp.ui.screens.tags

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagWithWishCount
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.TagsRepository
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DispatcherProvider
import timber.log.Timber

/**
 * Удаление ярлыков с «Вернуть». Пока снэкбар на экране, ярлык спрятан, но в базе он есть вместе со
 * связями и цветом, поэтому «Вернуть» ничего не восстанавливает, а просто показывает его снова.
 *
 * Снэкбар живёт над навигацией и переживает экран управления, поэтому удаление идёт не в области
 * ViewModel, а здесь. Вызывается только с главного потока.
 */
@Singleton
class TagDeleteQueue @Inject constructor(
    private val tagsRepository: TagsRepository,
    dispatcherProvider: DispatcherProvider,
) {
    private val scope = CoroutineScope(
        SupervisorJob() + dispatcherProvider.main() + CoroutineExceptionHandler { _, throwable ->
            if (BuildConfig.DEBUG) throw throwable
            FirebaseCrashlytics.getInstance().recordException(throwable)
            Timber.e(throwable.toString())
        },
    )

    private val _hiddenIds = MutableStateFlow<Set<String>>(emptySet())

    /** Ярлыки, удалённые с «Вернуть»: их не видно, хотя в базе они ещё есть. */
    val hiddenIds: StateFlow<Set<String>> = _hiddenIds.asStateFlow()

    /** Ярлыки с числом хотелок без спрятанных. */
    fun observeVisibleTags(): Flow<List<TagWithWishCount>> =
        combine(tagsRepository.observeAllTagsWithWishesCount(), hiddenIds) { tags, hidden ->
            tags.filter { it.tag.id !in hidden }
        }

    fun hide(tagId: String) {
        _hiddenIds.update { it + tagId }
    }

    fun undo(tagId: String) {
        _hiddenIds.update { it - tagId }
    }

    /** Снэкбар закрылся без «Вернуть»: удаляем из базы и только потом перестаём прятать. */
    fun commit(tagId: String) {
        if (tagId !in _hiddenIds.value) return
        scope.launch {
            try {
                tagsRepository.deleteTagsByIds(listOf(tagId))
            } finally {
                _hiddenIds.update { it - tagId }
            }
        }
    }
}
