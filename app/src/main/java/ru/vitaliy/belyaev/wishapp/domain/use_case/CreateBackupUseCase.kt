package ru.vitaliy.belyaev.wishapp.domain.use_case

import android.content.Context
import java.io.File
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository

internal class CreateBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val dataStoreRepository: DataStoreRepository
) {

    suspend operator fun invoke(
        context: Context,
        backupFile: File,
    ) {

        backupRepository.uploadNewBackup(
            context = context,
            backupFile = backupFile,
        )

        dataStoreRepository.updateIsBackupWasCreated(true)
    }
}