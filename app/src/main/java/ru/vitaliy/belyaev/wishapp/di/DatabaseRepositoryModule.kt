package ru.vitaliy.belyaev.wishapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.model.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.DatabaseRepository
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.WishesRepository
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DispatcherProvider

@InstallIn(SingletonComponent::class)
@Module
object DatabaseRepositoryModule {

    @Singleton
    @Provides
    fun bindDatabaseRepository(db: WishAppDb, dispatcherProvider: DispatcherProvider): WishesRepository {
        return DatabaseRepository(db, dispatcherProvider)
    }
}