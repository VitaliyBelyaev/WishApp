package ru.vitaliy.belyaev.wishapp.domain.repository

import android.content.Intent

internal interface BackupAuthRepository {

    fun isSignedIn(): Boolean

    /** Адрес аккаунта, в который вошли, или `null`. */
    fun signedInEmail(): String?

    /** Фото профиля аккаунта или `null`: его нет, или вошли до того, как приложение стало его просить. */
    fun signedInPhotoUrl(): String?

    /** Имя из профиля Google или `null`, как и фото: только у вошедших с запросом профиля. */
    fun signedInName(): String?

    suspend fun signInSilently()

    suspend fun signOut()

    suspend fun disconnectAccount()

    suspend fun checkIsSignedInFromIntent(intent: Intent): Boolean

    fun getSignInIntent(): Intent
}
