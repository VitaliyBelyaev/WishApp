package ru.vitaliy.belyaev.wishapp.shared.data

import app.cash.sqldelight.db.SqlDriver
import ru.vitaliy.belyaev.wishapp.shared.data.coroutines.getDispatcherProvider
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.shared.data.repository.DatabaseRepository

class WishAppSdk(databaseDriveFactory: DatabaseDriverFactory) {

    val databaseName: String = Config.DATABASE_NAME

    val databaseRepository: DatabaseRepository

    private val sqlDriver: SqlDriver = databaseDriveFactory.createDatabaseDriver()

    init {
        val database = WishAppDb(sqlDriver)
        databaseRepository = DatabaseRepository(database, getDispatcherProvider())
    }

    fun closeDatabase() {
        sqlDriver.close()
    }
}