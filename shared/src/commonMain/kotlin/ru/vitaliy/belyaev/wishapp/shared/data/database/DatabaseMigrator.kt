package ru.vitaliy.belyaev.wishapp.shared.data.database

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.use

object DatabaseMigrator {

    fun updateWishPositionsAfterMigrationFrom1To2(sqlDriver: SqlDriver) {
        val sqlCursor = sqlDriver.executeQuery(null, "SELECT * FROM Wish", 0)
        val allWishesInDbOrder = mutableListOf<Wish>()
        sqlCursor.use { cursor ->
            while (cursor.next()) {
                val wish = Wish(
                    wishId = cursor.getString(0)!!,
                    title = cursor.getString(1)!!,
                    link = cursor.getString(2)!!,
                    comment = cursor.getString(3)!!,
                    isCompleted = cursor.getLong(4)!! == 1L,
                    createdTimestamp = cursor.getLong(5)!!,
                    updatedTimestamp = cursor.getLong(6)!!,
                    position = -1
                )
                allWishesInDbOrder.add(wish)
            }
        }
        allWishesInDbOrder
            .filter { !it.isCompleted }
            .forEachIndexed { index, wish ->
                sqlDriver.execute(
                    identifier = null,
                    sql = "UPDATE Wish SET position = ? WHERE  wishId = ?",
                    parameters = 2
                ) {
                    bindLong(1, index.toLong())
                    bindString(2, wish.wishId)
                }
            }
    }
}