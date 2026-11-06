package ru.vitaliy.belyaev.wishapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.vitaliy.belyaev.wishapp.data.repository.remoteconfig.FirebaseRemoteConfigRepository
import ru.vitaliy.belyaev.wishapp.domain.repository.RemoteConfigRepository

@InstallIn(SingletonComponent::class)
@Module
interface RemoteConfigModule {

    @Binds
    fun bindRemoteConfigRepository(impl: FirebaseRemoteConfigRepository): RemoteConfigRepository
}
