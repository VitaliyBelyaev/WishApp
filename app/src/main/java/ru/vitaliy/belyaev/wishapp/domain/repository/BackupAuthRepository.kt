package ru.vitaliy.belyaev.wishapp.domain.repository

import android.content.Intent

internal interface BackupAuthRepository {

    fun isSignedIn(): Boolean

    suspend fun signInSilently()

    suspend fun checkIsSignedInFromIntent(intent: Intent): Boolean

    fun getSignInIntent(): Intent
}