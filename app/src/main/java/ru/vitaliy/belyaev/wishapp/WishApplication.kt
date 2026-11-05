package ru.vitaliy.belyaev.wishapp

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import com.google.android.material.color.DynamicColors
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.data.image.ImageRefFetcher
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AmplitudeWrapper
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.utils.isAndroidVersionSOrAbove
import timber.log.Timber

@HiltAndroidApp
class WishApplication : Application() {

    @Inject
    lateinit var imagesRepository: Lazy<ImagesRepository>

    override fun onCreate() {
        super.onCreate()

        if (isAndroidVersionSOrAbove) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        AmplitudeWrapper.init(applicationContext)

        SingletonImageLoader.setSafe {
            ImageLoader.Builder(applicationContext)
                .diskCachePolicy(CachePolicy.DISABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .components { add(ImageRefFetcher.Factory(imagesRepository)) }
                .build()
        }
    }
}