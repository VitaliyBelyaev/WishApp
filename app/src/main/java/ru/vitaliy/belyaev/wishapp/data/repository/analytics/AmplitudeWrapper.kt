package ru.vitaliy.belyaev.wishapp.data.repository.analytics

import android.content.Context
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.DefaultTrackingOptions
import ru.vitaliy.belyaev.wishapp.BuildConfig
import timber.log.Timber

object AmplitudeWrapper {

    var amplitude: Amplitude? = null

    fun init(context: Context) {
        val apiKey = BuildConfig.AMPLITUDE_API_KEY
        if (apiKey.isBlank()) {
            // Ключ есть только в release: debug и autotest в Amplitude не пишут
            Timber.i("Amplitude API key is not set, events go to log only")
            return
        }

        amplitude = Amplitude(
            Configuration(
                apiKey = apiKey,
                context = context,
                defaultTracking = DefaultTrackingOptions(
                    sessions = true,
                    appLifecycles = true,
                ),
            ),
        )
    }
}
