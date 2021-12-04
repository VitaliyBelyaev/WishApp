package ru.vitaliy.belyaev.wishapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.DatabaseRepository
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.WishesRepository

@InstallIn(SingletonComponent::class)
@Module
interface DatabaseRepositoryModule {

    @Singleton
    @Binds
    fun bindDatabaseRepository(impl: DatabaseRepository): WishesRepository
}