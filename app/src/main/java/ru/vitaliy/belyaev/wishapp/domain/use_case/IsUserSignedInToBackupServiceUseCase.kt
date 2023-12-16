package ru.vitaliy.belyaev.wishapp.domain.use_case

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupAuthRepository
import timber.log.Timber

internal class IsUserSignedInToBackupServiceUseCase @Inject constructor(
    private val backupAuthRepository: BackupAuthRepository,
) {

    suspend operator fun invoke(): Boolean {
        val haveSignedInAccountOnDevice = backupAuthRepository.isSignedIn()
        if (haveSignedInAccountOnDevice) {
            return true
        }
        return try {
            backupAuthRepository.signInSilently()
            true
        } catch (e: Exception) {
            Timber.e(e)
            FirebaseCrashlytics.getInstance().recordException(e)
            false
        }
    }
}