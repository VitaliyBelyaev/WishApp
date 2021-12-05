package ru.vitaliy.belyaev.wishapp.model.repository.tags

import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.model.database.Tag

interface TagsRepository {

    fun insertTag(title: String)

    fun updateTagTitle(title: String, tagId: Long)

    suspend fun getAllTags(): List<Tag>
    fun observeAllTags(): Flow<List<Tag>>

    fun deleteTagsByIds(ids: List<Long>)

    fun clearAllTags()
}