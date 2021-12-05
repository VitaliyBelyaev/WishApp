package ru.vitaliy.belyaev.wishapp.di

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.model.database.Config
import ru.vitaliy.belyaev.wishapp.model.database.WishAppDb

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): WishAppDb {
        val sqlDriver = AndroidSqliteDriver(
            WishAppDb.Schema,
            appContext,
            Config.DATABASE_NAME
        )

        return WishAppDb(sqlDriver)
    }
}