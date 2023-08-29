package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.BackupAndRestoreScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository
import ru.vitaliy.belyaev.wishapp.domain.use_case.CreateBackupUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.RestoreBackupUseCase
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel

@HiltViewModel
internal class BackupViewModel @Inject constructor(
    private val createBackupUseCase: CreateBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    private val backupRepository: BackupRepository,
    private val analyticsRepository: AnalyticsRepository
) : BaseViewModel() {

    fun trackScreenShow() {
        analyticsRepository.trackEvent(BackupAndRestoreScreenShowEvent)
    }
}