package ru.vitaliy.belyaev.wishapp.domain.use_case

import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupAuthRepository
import timber.log.Timber

internal class IsUserSignedInToBackupServiceUseCase @Inject constructor(
    private val backupAuthRepository: BackupAuthRepository,
) {

    suspend operator fun invoke(): Boolean {
        val haveSignedInAccountOnDevice = backupAuthRepository.isSignedIn()

        Timber.tag("RTRT").d("IsUserSignedInToBackupServiceUseCase: haveSignedInAccountOnDevice = $haveSignedInAccountOnDevice")
        if (haveSignedInAccountOnDevice) {
            return true
        }

        return try {
            Timber.tag("RTRT").d("IsUserSignedInToBackupServiceUseCase: signInSilently")
            backupAuthRepository.signInSilently()
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }
}