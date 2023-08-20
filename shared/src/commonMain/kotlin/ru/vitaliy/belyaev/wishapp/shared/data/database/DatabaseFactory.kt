package ru.vitaliy.belyaev.wishapp.shared.data.database

import com.squareup.sqldelight.db.SqlDriver

object DatabaseFactory {

    fun createDatabase(sqlDriver: SqlDriver): WishAppDb {
        return WishAppDb(sqlDriver)
    }
}