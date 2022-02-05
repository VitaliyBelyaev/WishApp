package ru.vitaliy.belyaev.wishapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.data.repository.DatabaseRepository
import ru.vitaliy.belyaev.wishapp.data.repository.tags.TagsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.wishes.WishesRepository
import ru.vitaliy.belyaev.wishapp.data.repository.wishtagrelation.WishTagRelationRepository

@InstallIn(SingletonComponent::class)
@Module
interface DatabaseRepositoryModule {

    @Singleton
    @Binds
    fun bindWishesRepositoryRepository(impl: DatabaseRepository): WishesRepository

    @Singleton
    @Binds
    fun bindWishTagRelationRepositoryRepository(impl: DatabaseRepository): WishTagRelationRepository

    @Singleton
    @Binds
    fun bindTagsRepositoryRepository(impl: DatabaseRepository): TagsRepository
}