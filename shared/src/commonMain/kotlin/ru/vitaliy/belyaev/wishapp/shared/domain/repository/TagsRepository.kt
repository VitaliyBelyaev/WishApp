package ru.vitaliy.belyaev.wishapp.shared.domain.repository

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagWithWishCount

interface TagsRepository {

    @NativeCoroutines
    suspend fun insertTag(title: String): String

    @NativeCoroutines
    suspend fun updateTagTitle(title: String, tagId: String)

    @NativeCoroutines
    suspend fun getTagById(id: String): TagEntity

    @NativeCoroutines
    suspend fun getAllTags(): List<TagEntity>

    @NativeCoroutines
    fun observeAllTags(): Flow<List<TagEntity>>

    @NativeCoroutines
    fun observeTagsByWishId(wishId: String): Flow<List<TagEntity>>

    @NativeCoroutines
    fun observeAllTagsWithWishesCount(): Flow<List<TagWithWishCount>>

    @NativeCoroutines
    suspend fun deleteTagsByIds(ids: List<String>)

    @NativeCoroutines
    suspend fun clearAllTags()
}