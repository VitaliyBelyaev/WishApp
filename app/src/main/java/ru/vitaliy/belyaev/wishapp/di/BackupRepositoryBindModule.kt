package ru.vitaliy.belyaev.wishapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.vitaliy.belyaev.wishapp.data.repository.GoogleDriveBackupRepository
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository

@InstallIn(SingletonComponent::class)
@Module
interface BackupRepositoryBindModule {

    @Binds
    fun bindBackupRepository(impl: GoogleDriveBackupRepository): BackupRepository
}