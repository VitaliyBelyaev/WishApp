package ru.vitaliy.belyaev.wishapp.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.vitaliy.belyaev.wishapp.data.repository.backup.GoogleBackupAuthRepository
import ru.vitaliy.belyaev.wishapp.data.repository.backup.GoogleDriveBackupRepository
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupAuthRepository
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository

@InstallIn(SingletonComponent::class)
@Module
internal object BackupRepositoryProvideModule {

    @Provides
    fun provideBackupRepository(@ApplicationContext context: Context): BackupRepository {
        return GoogleDriveBackupRepository(context)
    }

    @Provides
    fun provideBackupAuthRepository(@ApplicationContext context: Context): BackupAuthRepository {
        return GoogleBackupAuthRepository(context)
    }
}