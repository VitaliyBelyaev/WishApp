package ru.vitaliy.belyaev.wishapp.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagWithWishCount

interface TagsRepository {

    fun insertTag(title: String): String

    fun updateTagTitle(title: String, tagId: String)

    suspend fun getAllTags(): List<TagEntity>
    fun observeAllTags(): Flow<List<TagEntity>>
    fun observeTagsByWishId(wishId: String): Flow<List<TagEntity>>

    fun observeAllTagsWithWishesCount(): Flow<List<TagWithWishCount>>

    fun deleteTagsByIds(ids: List<String>)

    fun clearAllTags()
}