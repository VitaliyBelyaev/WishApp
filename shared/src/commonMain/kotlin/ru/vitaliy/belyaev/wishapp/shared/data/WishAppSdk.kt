package ru.vitaliy.belyaev.wishapp.shared.data

import app.cash.sqldelight.db.SqlDriver
import io.github.aakira.napier.Napier
import ru.vitaliy.belyaev.wishapp.shared.data.coroutines.getDispatcherProvider
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.data.database.Image
import ru.vitaliy.belyaev.wishapp.shared.data.database.ImageQueries
import ru.vitaliy.belyaev.wishapp.shared.data.database.Tag
import ru.vitaliy.belyaev.wishapp.shared.data.database.TagQueries
import ru.vitaliy.belyaev.wishapp.shared.data.database.Wish
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishQueries
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishTagRelation
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

    fun getDatabaseRepository(): DatabaseRepository {
        Napier.d("getDatabaseRepository")

        return _databaseRepository ?: reopenDatabase().let { _databaseRepository!! }
    }

    fun reopenDatabase() {
        sqlDriver = databaseDriveFactory.createDatabaseDriver(Config.DATABASE_NAME)
        val database = WishAppDb(sqlDriver!!).also {
            this.database = it
        }
        _databaseRepository = DatabaseRepository(database, getDispatcherProvider())
    }

    fun copyContentFromBackupDatabase(backupDbName: String): Boolean {
        Napier.d("copyContentFromBackupDatabase, backupDbName: $backupDbName")

        var sqlDriverTemp: SqlDriver? = null

        return try {

            sqlDriverTemp = databaseDriveFactory.createDatabaseDriver(backupDbName)
            val databaseTemp = WishAppDb(sqlDriverTemp)

            val allWishes: List<Wish> = databaseTemp.wishQueries.getAllForBackup().executeAsList()
            allWishes.forEach { wish ->
                wishQueries?.insertOrReplace(
                    wishId = wish.wishId,
                    title = wish.title,
                    link = wish.link,
                    comment = wish.comment,
                    isCompleted = wish.isCompleted,
                    createdTimestamp = wish.createdTimestamp,
                    updatedTimestamp = wish.updatedTimestamp,
                    position = wish.position
                )
            }

            val allTags: List<Tag> = databaseTemp.tagQueries.getAllForBackup().executeAsList()
            allTags.forEach { tag ->
                tagQueries?.insertOrReplace(
                    tagId = tag.tagId,
                    title = tag.title
                )
            }

            val allImages: List<Image> = databaseTemp.imageQueries.getAllForBackup().executeAsList()
            allImages.forEach { image ->
                imageQueries?.insertOrReplace(
                    id = image.id,
                    wishId = image.wishId,
                    rawData = image.rawData
                )
            }

            val allRelations: List<WishTagRelation> = databaseTemp.wishTagRelationQueries
                .getAllForBackup().executeAsList()
            allRelations.forEach { relation ->
                wishTagRelationQueries?.insertOrReplace(
                    wishId = relation.wishId,
                    tagId = relation.tagId
                )
            }

            sqlDriverTemp.close()
            true
        } catch (error: Throwable) {
            Napier.e(message = "Error while copying content from backup database", throwable = error)
            false
        } finally {
            sqlDriverTemp?.close()
        }
    }
}