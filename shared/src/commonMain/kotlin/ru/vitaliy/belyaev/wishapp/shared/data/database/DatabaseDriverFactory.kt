package ru.vitaliy.belyaev.wishapp.shared.data.database

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {

    fun createDatabaseDriver(): SqlDriver
}