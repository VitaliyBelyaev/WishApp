package ru.vitaliy.belyaev.wishapp.shared.data

import ru.vitaliy.belyaev.wishapp.shared.data.coroutines.getDispatcherProvider
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.shared.data.repository.DatabaseRepository

class WishAppSdk(databaseDriveFactory: DatabaseDriverFactory) {

    val databaseRepository: DatabaseRepository

    init {
        val database = WishAppDb(databaseDriveFactory.createDatabaseDriver())
        databaseRepository = DatabaseRepository(database, getDispatcherProvider())
    }
}