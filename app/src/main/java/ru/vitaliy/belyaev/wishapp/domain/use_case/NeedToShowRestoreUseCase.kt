package ru.vitaliy.belyaev.wishapp.domain.use_case

import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository

internal class NeedToShowRestoreUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
) {

    suspend operator fun invoke(): Boolean {
        val isRestoreFromBackupWasSucceed: Boolean =
            dataStoreRepository.isRestoreFromBackupWasSucceed.firstOrNull() ?: false

        val isBackupWasCreated: Boolean =
            dataStoreRepository.isBackupWasCreated.firstOrNull() ?: false

        return !isRestoreFromBackupWasSucceed && !isBackupWasCreated
    }
}