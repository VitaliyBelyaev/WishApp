package ru.vitaliy.belyaev.wishapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.vitaliy.belyaev.wishapp.model.repository.DatabaseRepository
import ru.vitaliy.belyaev.wishapp.model.repository.WishAppDatabaseRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface DatabaseRepositoryModule {

    @Singleton
    @Binds
    fun bindDatabaseRepository(impl: WishAppDatabaseRepository): DatabaseRepository
}