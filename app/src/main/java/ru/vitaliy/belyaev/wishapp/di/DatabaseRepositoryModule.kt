package ru.vitaliy.belyaev.wishapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.TagsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishTagRelationRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository

@InstallIn(SingletonComponent::class)
@Module
object DatabaseRepositoryModule {

    @Singleton
    @Provides
    fun provideWishesRepositoryRepository(
        wishAppSdk: WishAppSdk
    ): WishesRepository = wishAppSdk.getDatabaseRepository()

    @Singleton
    @Provides
    fun provideTagsRepositoryRepository(
        wishAppSdk: WishAppSdk
    ): TagsRepository = wishAppSdk.getDatabaseRepository()

    @Singleton
    @Provides
    fun provideWishTagRelationRepository(
        wishAppSdk: WishAppSdk
    ): WishTagRelationRepository = wishAppSdk.getDatabaseRepository()

    @Singleton
    @Provides
    fun provideImagesRepository(
        wishAppSdk: WishAppSdk
    ): ImagesRepository = wishAppSdk.getDatabaseRepository()
}