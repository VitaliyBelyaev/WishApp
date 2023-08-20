package ru.vitaliy.belyaev.wishapp.shared.data.database

import com.squareup.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {

    fun createDatabaseDriver(): SqlDriver
}