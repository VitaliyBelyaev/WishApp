package ru.vitaliy.belyaev.wishapp.model.repository.tags

import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.model.database.Tag

interface TagsRepository {

    fun insertTag(tag: Tag)

    fun updateTagTitle(title: String, tagId: String)

    suspend fun getAllTags(): List<Tag>
    fun observeAllTags(): Flow<List<Tag>>
    fun observeTagsByWishId(wishId: String): Flow<List<Tag>>

    fun deleteTagsByIds(ids: List<String>)

    fun clearAllTags()
}