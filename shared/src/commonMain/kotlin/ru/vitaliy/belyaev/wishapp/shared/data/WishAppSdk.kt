package ru.vitaliy.belyaev.wishapp.shared.data

import app.cash.sqldelight.db.SqlDriver
import io.github.aakira.napier.Napier
import ru.vitaliy.belyaev.wishapp.shared.data.coroutines.getDispatcherProvider
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.data.database.ImageQueries
import ru.vitaliy.belyaev.wishapp.shared.data.database.TagQueries
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishQueries
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishTagRelationQueries
import ru.vitaliy.belyaev.wishapp.shared.data.repository.DatabaseRepository

class WishAppSdk(private val databaseDriveFactory: DatabaseDriverFactory) {

    val databaseName: String = Config.DATABASE_NAME

    private var _databaseRepository: DatabaseRepository? = null

    private var sqlDriver: SqlDriver? = null

    private var database: WishAppDb? = null

    private val wishQueries: WishQueries?
        get() = database?.wishQueries

    private val wishTagRelationQueries: WishTagRelationQueries?
        get() = database?.wishTagRelationQueries

    private val tagQueries: TagQueries?
        get() = database?.tagQueries

    private val imageQueries: ImageQueries?
        get() = database?.imageQueries

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
        val database = WishAppDb(sqlDriver!!).also {
            this.database = it
        }
        _databaseRepository = DatabaseRepository(database, getDispatcherProvider())
    }

    fun copyContentFromBackupDatabase(backupDbName: String): Boolean {
        Napier.d("copyContentFromBackupDatabase, backupDbName: $backupDbName")


        return try {
            attachBackupDatabase(backupDbName)

            wishQueries?.transaction {
                wishQueries?.insertFromBackupDb()
            }

            detachBackupDatabase()
            true
        } catch (error: Throwable) {
            Napier.e(message = "Error while copying content from backup database", throwable = error)
            false
        }
    }

    fun attachBackupDatabase(backupDbName: String) {
        Napier.d("attachBackupDatabase, backupDbName: $backupDbName")

        sqlDriver!!.execute(
            identifier = null,
            sql = "ATTACH DATABASE ? AS $BACKUP_DATABASE_SCHEMA_NAME",
            parameters = 1
        ) {
            bindString(0, backupDbName)
        }
    }

    fun detachBackupDatabase() {
        Napier.d("detachBackupDatabase")

        sqlDriver!!.execute(
            identifier = null,
            sql = "DETACH DATABASE $BACKUP_DATABASE_SCHEMA_NAME",
            parameters = 0
        )
    }

    companion object {

        private const val BACKUP_DATABASE_SCHEMA_NAME = "backupDb"
    }
}