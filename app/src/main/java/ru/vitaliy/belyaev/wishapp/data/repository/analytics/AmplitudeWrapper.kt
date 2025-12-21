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
            Timber.e("Amplitude API key is not set")
            return
        }

        amplitude = Amplitude(
            Configuration(
                apiKey = apiKey,
                context = context,
                defaultTracking = DefaultTrackingOptions(
                    sessions = true,
                    appLifecycles = true,
                )
            )
        )
    }
}