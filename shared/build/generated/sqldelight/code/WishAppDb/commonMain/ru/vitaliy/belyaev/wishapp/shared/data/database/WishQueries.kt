package ru.vitaliy.belyaev.wishapp.shared.`data`.database

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.Transacter

public interface WishQueries : Transacter {
    public fun <T : Any> getById(
        wishId: String, mapper: (
            wishId: String,
            title: String,
            link: String,
            comment: String,
            isCompleted: Boolean,
            createdTimestamp: Long,
            updatedTimestamp: Long,
            position: Long
        ) -> T
    ): Query<T>

    public fun getById(wishId: String): Query<Wish>

    public fun <T : Any> getAll(
        isCompleted: Boolean, mapper: (
            wishId: String,
            title: String,
            link: String,
            comment: String,
            isCompleted: Boolean,
            createdTimestamp: Long,
            updatedTimestamp: Long,
            position: Long
        ) -> T
    ): Query<T>

    public fun getAll(isCompleted: Boolean): Query<Wish>

    public fun <T : Any> getAllNotCompleted(
        mapper: (
            wishId: String,
            title: String,
            link: String,
            comment: String,
            isCompleted: Boolean,
            createdTimestamp: Long,
            updatedTimestamp: Long,
            position: Long
        ) -> T
    ): Query<T>

    public fun getAllNotCompleted(): Query<Wish>

    public fun <T : Any> getAllCompleted(
        mapper: (
            wishId: String,
            title: String,
            link: String,
            comment: String,
            isCompleted: Boolean,
            createdTimestamp: Long,
            updatedTimestamp: Long,
            position: Long
        ) -> T
    ): Query<T>

    public fun getAllCompleted(): Query<Wish>

    public fun getWishesCountWithValidPosition(): Query<Long>

    public fun getWishesCount(isCompleted: Boolean): Query<Long>

    public fun insert(
        wishId: String,
        title: String,
        link: String,
        comment: String,
        isCompleted: Boolean,
        createdTimestamp: Long,
        updatedTimestamp: Long,
        position: Long
    ): Unit

    public fun updateTitle(
        title: String,
        updatedTimestamp: Long,
        wishId: String
    ): Unit

    public fun updateLink(
        link: String,
        updatedTimestamp: Long,
        wishId: String
    ): Unit

    public fun updateComment(
        comment: String,
        updatedTimestamp: Long,
        wishId: String
    ): Unit

    public fun updateIsCompleted(
        isCompleted: Boolean,
        updatedTimestamp: Long,
        wishId: String
    ): Unit

    public fun updatePosition(position: Long, wishId: String): Unit

    public fun updatePositionsOnItemMoveDown(position: Long, position_: Long): Unit

    public fun updatePositionsOnItemMoveUp(position: Long, position_: Long): Unit

    public fun deleteByIds(wishId: Collection<String>): Unit

    public fun deleteById(wishId: String): Unit

    public fun updatePositionsOnDelete(position: Long): Unit

    public fun clear(): Unit
}
