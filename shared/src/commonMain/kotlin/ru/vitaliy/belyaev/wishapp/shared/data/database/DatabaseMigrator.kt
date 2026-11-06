package ru.vitaliy.belyaev.wishapp.shared.data.database

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.normalizeTagTitle

object DatabaseMigrator {

    /** Шаги после `.sqm`, которые нельзя сделать на SQL. Общие для Android и iOS. */
    val afterVersionCallbacks: Array<AfterVersion> = arrayOf(
        AfterVersion(1) { updateWishPositionsAfterMigrationFrom1To2(it) },
        AfterVersion(3) { mergeDuplicateTagsAfterMigrationFrom3To4(it) },
    )

    fun updateWishPositionsAfterMigrationFrom1To2(sqlDriver: SqlDriver) {

        val allWishes: List<Wish> = sqlDriver.executeQuery(
            identifier = null,
            sql = "SELECT * FROM Wish",
            mapper = { cursor ->
                val allWishesInDbOrder = mutableListOf<Wish>()
                while (cursor.next().value) {
                    val wish = Wish(
                        wishId = cursor.getString(0)!!,
                        title = cursor.getString(1)!!,
                        link = cursor.getString(2)!!,
                        comment = cursor.getString(3)!!,
                        isCompleted = cursor.getLong(4)!! == 1L,
                        createdTimestamp = cursor.getLong(5)!!,
                        updatedTimestamp = cursor.getLong(6)!!,
                        position = -1,
                        completedTimestamp = null
                    )
                    allWishesInDbOrder.add(wish)
                }
                QueryResult.Value(allWishesInDbOrder)
            },
            parameters = 0
        ).value

        allWishes
            .filter { !it.isCompleted }
            .forEachIndexed { index, wish ->
                sqlDriver.execute(
                    identifier = null,
                    sql = "UPDATE Wish SET position = ? WHERE  wishId = ?",
                    parameters = 2
                ) {
                    bindLong(0, index.toLong())
                    bindString(1, wish.wishId)
                }
            }
    }

    /**
     * Объединяет ярлыки, которые отличаются только регистром и пробелами по краям: «Дом» и «дом».
     * Сравнение в Kotlin, потому что SQLite lower() не знает кириллицу.
     *
     * Остаётся ярлык с большим числом хотелок, при равенстве добавленный раньше. Связи остальных
     * переносятся на него через INSERT OR IGNORE: хотелка может быть с обоими ярлыками сразу.
     */
    fun mergeDuplicateTagsAfterMigrationFrom3To4(sqlDriver: SqlDriver) {
        val tags: List<TagForMerge> = sqlDriver.executeQuery(
            identifier = null,
            sql = """
                SELECT tag.tagId, tag.title, tag.rowid, count(relation.wishId)
                FROM Tag tag
                LEFT JOIN WishTagRelation relation ON relation.tagId = tag.tagId
                GROUP BY tag.tagId
            """.trimIndent(),
            mapper = { cursor ->
                val result = mutableListOf<TagForMerge>()
                while (cursor.next().value) {
                    result.add(
                        TagForMerge(
                            tagId = cursor.getString(0)!!,
                            title = cursor.getString(1)!!,
                            order = cursor.getLong(2)!!,
                            wishesCount = cursor.getLong(3)!!,
                        )
                    )
                }
                QueryResult.Value(result)
            },
            parameters = 0
        ).value

        tags
            .groupBy { normalizeTagTitle(it.title) }
            .values
            .filter { it.size > 1 }
            .forEach { duplicates ->
                val kept = duplicates
                    .sortedWith(compareByDescending<TagForMerge> { it.wishesCount }.thenBy { it.order })
                    .first()
                duplicates
                    .filter { it.tagId != kept.tagId }
                    .forEach { duplicate -> mergeTagInto(sqlDriver, duplicate.tagId, kept.tagId) }
            }
    }

    private fun mergeTagInto(sqlDriver: SqlDriver, fromTagId: String, toTagId: String) {
        sqlDriver.execute(
            identifier = null,
            sql = """
                INSERT OR IGNORE INTO WishTagRelation(wishId, tagId, isShown)
                SELECT wishId, ?, isShown FROM WishTagRelation WHERE tagId = ?
            """.trimIndent(),
            parameters = 2
        ) {
            bindString(0, toTagId)
            bindString(1, fromTagId)
        }
        sqlDriver.execute(null, "DELETE FROM WishTagRelation WHERE tagId = ?", 1) { bindString(0, fromTagId) }
        sqlDriver.execute(null, "DELETE FROM Tag WHERE tagId = ?", 1) { bindString(0, fromTagId) }
    }

    private class TagForMerge(
        val tagId: String,
        val title: String,
        val order: Long,
        val wishesCount: Long,
    )
}
