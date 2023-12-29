package ru.vitaliy.belyaev.wishapp.shared.data.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver

object DatabaseMigrator {

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
                        position = -1
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
                    bindLong(1, index.toLong())
                    bindString(2, wish.wishId)
                }
            }
    }
}