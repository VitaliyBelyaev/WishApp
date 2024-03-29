package ru.vitaliy.belyaev.wishapp.shared.data

import app.cash.sqldelight.db.SqlDriver
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import ru.vitaliy.belyaev.wishapp.shared.data.coroutines.getDispatcherProvider
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.shared.data.repository.DatabaseRepository

class WishAppSdk(private val databaseDriveFactory: DatabaseDriverFactory) {

    val databaseName: String = Config.DATABASE_NAME

    private var _databaseRepository: DatabaseRepository? = null

    private var sqlDriver: SqlDriver? = null

    init {

        Napier.d("WishAppSdk init")
        reopenDatabase()
    }

    fun closeDatabase() {
        Napier.d("closeDatabase")
        sqlDriver?.close()
        sqlDriver = null
        _databaseRepository = null
    }

    fun getDatabaseRepository(): DatabaseRepository {
        Napier.d("getDatabaseRepository")

        return _databaseRepository ?: reopenDatabase().let { _databaseRepository!! }
    }

    fun reopenDatabase() {

        Napier.d("Reopen database")
        closeDatabase()
        sqlDriver = databaseDriveFactory.createDatabaseDriver()
        val database = WishAppDb(sqlDriver!!)
        _databaseRepository = DatabaseRepository(database, getDispatcherProvider())
    }
}