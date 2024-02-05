package ru.vitaliy.belyaev.wishapp.shared.data

import app.cash.sqldelight.db.SqlDriver
import ru.vitaliy.belyaev.wishapp.shared.data.coroutines.getDispatcherProvider
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.shared.data.repository.DatabaseRepository

class WishAppSdk(private val databaseDriveFactory: DatabaseDriverFactory) {

    val databaseName: String = Config.DATABASE_NAME

    lateinit var databaseRepository: DatabaseRepository

    private lateinit var sqlDriver: SqlDriver

    init {
        reopenDatabase()
    }

    fun closeDatabase() {
        sqlDriver.close()
    }

    fun reopenDatabase() {
        sqlDriver = databaseDriveFactory.createDatabaseDriver()
        val database = WishAppDb(sqlDriver)
        databaseRepository = DatabaseRepository(database, getDispatcherProvider())
    }
}