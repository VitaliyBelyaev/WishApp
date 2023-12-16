package ru.vitaliy.belyaev.wishapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupAuthRepository
import ru.vitaliy.belyaev.wishapp.domain.use_case.IsUserSignedInToBackupServiceUseCase
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository

//@InstallIn(ActivityComponent::class)
//@Module
//internal object UseCaseProvideModule {
//
//    @Provides
//    fun provideUseCase1(
//        backupAuthRepository: BackupAuthRepository
//    ): IsUserSignedInToBackupServiceUseCase = IsUserSignedInToBackupServiceUseCase(
//        backupAuthRepository
//    )
//
//
//}