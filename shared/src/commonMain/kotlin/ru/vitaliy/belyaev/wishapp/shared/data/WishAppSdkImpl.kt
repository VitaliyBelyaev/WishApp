package ru.vitaliy.belyaev.wishapp.shared.data

import ru.vitaliy.belyaev.wishapp.shared.data.coroutines.getDispatcherProvider
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.shared.data.repository.DatabaseRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.TagsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishTagRelationRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository

class WishAppSdk(databaseDriveFactory: DatabaseDriverFactory) {

    private val databaseRepository: DatabaseRepository

    init {
        val database = WishAppDb(databaseDriveFactory.createDatabaseDriver())
        databaseRepository = DatabaseRepository(database, getDispatcherProvider())
    }

    val wishesRepository: WishesRepository = databaseRepository
    val tagsRepository: TagsRepository = databaseRepository
    val wishTagRelationRepository: WishTagRelationRepository = databaseRepository
}