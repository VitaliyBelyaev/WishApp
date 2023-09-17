package ru.vitaliy.belyaev.wishapp.shared.data.database

import app.cash.sqldelight.db.SqlDriver

object DatabaseFactory {

    fun createDatabase(sqlDriver: SqlDriver): WishAppDb {
        return WishAppDb(sqlDriver)
    }
}