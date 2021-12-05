package ru.vitaliy.belyaev.wishapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DefaultDispatcherProvider
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DispatcherProvider

@InstallIn(SingletonComponent::class)
@Module
interface DispatcherProviderModule {

    @Singleton
    @Binds
    fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider
}