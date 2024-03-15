package ru.vitaliy.belyaev.wishapp.shared.data

import app.cash.sqldelight.db.SqlDriver
import ru.vitaliy.belyaev.wishapp.shared.data.coroutines.getDispatcherProvider
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.shared.data.repository.DatabaseRepository

class WishAppSdk(private val databaseDriveFactory: DatabaseDriverFactory) {

    val databaseName: String = Config.DATABASE_NAME

    private var _databaseRepository: DatabaseRepository? = null

    private var sqlDriver: SqlDriver? = null

    init {
        reopenDatabase()
    }

    fun closeDatabase() {
        sqlDriver?.close()
        sqlDriver = null
        _databaseRepository = null
    }

    fun getDatabaseRepository(): DatabaseRepository {
        return _databaseRepository ?: reopenDatabase().let { _databaseRepository!! }
    }

    fun reopenDatabase() {
        closeDatabase()
        sqlDriver = databaseDriveFactory.createDatabaseDriver()
        val database = WishAppDb(sqlDriver!!)
        _databaseRepository = DatabaseRepository(database, getDispatcherProvider())
    }
}