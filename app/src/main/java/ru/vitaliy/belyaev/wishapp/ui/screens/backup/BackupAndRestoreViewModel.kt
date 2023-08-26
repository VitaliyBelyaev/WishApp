package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.entity.analytics.BackupAndRestoreScreenShowEvent
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel

@HiltViewModel
class BackupAndRestoreViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val analyticsRepository: AnalyticsRepository
) : BaseViewModel() {


    fun trackScreenShow() {
        analyticsRepository.trackEvent(BackupAndRestoreScreenShowEvent)
    }
}