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
    private val context: Context,
) : BackupAuthRepository {

    override fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    override fun signedInEmail(): String? {
        return GoogleSignIn.getLastSignedInAccount(context)?.email
    }

    override fun signedInPhotoUrl(): String? {
        return GoogleSignIn.getLastSignedInAccount(context)?.photoUrl?.toString()
    }

    override fun signedInName(): String? {
        return GoogleSignIn.getLastSignedInAccount(context)?.displayName?.takeIf { it.isNotBlank() }
    }

    override suspend fun signInSilently() {
        getGoogleSignInClient(context).silentSignIn().await()
    }

    override suspend fun signOut() {
        getGoogleSignInClient(context).signOut().await()
    }

    override suspend fun disconnectAccount() {
        getGoogleSignInClient(context).revokeAccess().await()
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
        // Профиль (фото) просим только при входе вручную: тихий вход с новой областью не прошёл бы
        // у тех, кто вошёл раньше, и их выкинуло бы из аккаунта
        return getGoogleSignInClient(context, withProfile = true).signInIntent
    }

    private fun getGoogleSignInClient(context: Context, withProfile: Boolean = false): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder()
            .requestEmail()
            .apply { if (withProfile) requestProfile() }
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()

        return GoogleSignIn.getClient(context, signInOptions)
    }
}
