package ru.vitaliy.belyaev.wishapp.shared.domain.repository

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagColor
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagWithWishCount

interface TagsRepository {

    /**
     * Создаёт ярлык с самым редким цветом. Если ярлык с таким названием уже есть, без учёта
     * регистра и пробелов по краям, новый не создаётся и возвращается id существующего.
     */
    @NativeCoroutines
    suspend fun insertTag(title: String): String

    /** `false`, если другой ярлык уже так называется: дубли запрещены, название не меняется. */
    @NativeCoroutines
    suspend fun updateTagTitle(title: String, tagId: String): Boolean

    @NativeCoroutines
    suspend fun updateTagColor(color: TagColor, tagId: String)

    @NativeCoroutines
    suspend fun getTagById(id: String): TagEntity

    @NativeCoroutines
    suspend fun getAllTags(): List<TagEntity>

    @NativeCoroutines
    fun observeAllTags(): Flow<List<TagEntity>>

    @NativeCoroutines
    suspend fun getTagsByWishId(wishId: String): List<TagEntity>

    @NativeCoroutines
    fun observeTagsByWishId(wishId: String): Flow<List<TagEntity>>

    @NativeCoroutines
    fun observeAllTagsWithWishesCount(): Flow<List<TagWithWishCount>>

    @NativeCoroutines
    suspend fun deleteTagsByIds(ids: List<String>)

    @NativeCoroutines
    suspend fun clearAllTags()
}
