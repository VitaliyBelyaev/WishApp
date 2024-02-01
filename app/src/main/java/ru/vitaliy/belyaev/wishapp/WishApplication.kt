package ru.vitaliy.belyaev.wishapp

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.request.CachePolicy
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AmplitudeWrapper
import ru.vitaliy.belyaev.wishapp.utils.isAndroidVersionSOrAbove
import timber.log.Timber

@HiltAndroidApp
class WishApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (isAndroidVersionSOrAbove) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        AmplitudeWrapper.init(applicationContext)

        Coil.setImageLoader {
            ImageLoader.Builder(applicationContext)
                .diskCachePolicy(CachePolicy.DISABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build()
        }
    }
}