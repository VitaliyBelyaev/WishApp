package ru.vitaliy.belyaev.wishapp.data.repository.backup

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.tasks.await
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupAuthRepository
import timber.log.Timber

internal class GoogleBackupAuthRepository(
    private val context: Context
) : BackupAuthRepository {

    override fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    override suspend fun signInSilently() {
        getGoogleSignInClient(context).silentSignIn().await()
    }

    override suspend fun checkIsSignedInFromIntent(intent: Intent): Boolean {
        return try {
            GoogleSignIn.getSignedInAccountFromIntent(intent).await()
            true
        } catch (e: Exception) {
            Timber.e(e, message = "Error while checking is user signed in from intent")
            false
        }
    }

    override fun getSignInIntent(): Intent {
        return getGoogleSignInClient(context).signInIntent
    }

    private fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder()
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()

        return GoogleSignIn.getClient(context, signInOptions)
    }
}