package ru.vitaliy.belyaev.wishapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AmplitudeAnalyticsRepository
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepositoryComposite
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.FirebaseAnalyticsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.LogAnalyticsRepository

@InstallIn(SingletonComponent::class)
@Module
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalyticsRepository(): AnalyticsRepository {
        val repositories = mutableListOf(
            FirebaseAnalyticsRepository(),
            AmplitudeAnalyticsRepository()
        )
        if (BuildConfig.DEBUG) {
            repositories.add(LogAnalyticsRepository())
        }

        return AnalyticsRepositoryComposite(repositories)
    }
}