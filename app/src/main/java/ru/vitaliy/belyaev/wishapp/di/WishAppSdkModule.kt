package ru.vitaliy.belyaev.wishapp.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory

@InstallIn(SingletonComponent::class)
@Module
object WishAppSdkModule {

    @Provides
    @Singleton
    fun provideWishAppSdk(@ApplicationContext appContext: Context): WishAppSdk {
        return WishAppSdk(DatabaseDriverFactory(appContext))
    }
}