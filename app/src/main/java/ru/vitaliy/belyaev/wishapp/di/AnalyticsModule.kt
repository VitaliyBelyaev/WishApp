package ru.vitaliy.belyaev.wishapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.model.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.model.repository.analytics.FirebaseAnalyticsRepository
import ru.vitaliy.belyaev.wishapp.model.repository.analytics.LogAnalyticsRepository

@InstallIn(SingletonComponent::class)
@Module
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalyticsRepository(): AnalyticsRepository {
        return if (BuildConfig.DEBUG) {
            LogAnalyticsRepository()
        } else {
            FirebaseAnalyticsRepository()
        }
    }
}