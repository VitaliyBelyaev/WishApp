package ru.vitaliy.belyaev.wishapp.data.repository.remoteconfig

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import javax.inject.Inject
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.domain.model.UiLookMode
import ru.vitaliy.belyaev.wishapp.domain.repository.RemoteConfigRepository
import timber.log.Timber

@Singleton
class FirebaseRemoteConfigRepository @Inject constructor() : RemoteConfigRepository {

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().apply {
            if (BuildConfig.DEBUG) {
                setConfigSettingsAsync(
                    FirebaseRemoteConfigSettings.Builder()
                        .setMinimumFetchIntervalInSeconds(DEBUG_FETCH_INTERVAL_SECONDS)
                        .build()
                )
            }
            setDefaultsAsync(mapOf(KEY_UI_LOOK_MODE to DEFAULT_UI_LOOK_MODE))
        }
    }

    // Пока значения по умолчанию не применились, getString вернёт пустую строку: это тоже old_only
    override fun activatedUiLookMode(): UiLookMode {
        return UiLookMode.fromRemoteValue(remoteConfig.getString(KEY_UI_LOOK_MODE))
    }

    override fun fetchForNextLaunch() {
        remoteConfig.fetchAndActivate()
            .addOnFailureListener {
                // Без сети это обычная ситуация, в Crashlytics не шлём
                Timber.w(it, "Remote Config fetch failed")
            }
            .addOnSuccessListener { updated ->
                if (updated) {
                    val mode = remoteConfig.getString(KEY_UI_LOOK_MODE)
                    FirebaseCrashlytics.getInstance().setCustomKey(KEY_UI_LOOK_MODE, mode)
                }
            }
    }

    private companion object {
        const val KEY_UI_LOOK_MODE = "ui_look_mode"
        const val DEFAULT_UI_LOOK_MODE = "old_only"
        const val DEBUG_FETCH_INTERVAL_SECONDS = 60L
    }
}
