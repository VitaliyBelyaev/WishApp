package ru.vitaliy.belyaev.wishapp.data.repository.analytics

import android.content.Context
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.DefaultTrackingOptions
import ru.vitaliy.belyaev.wishapp.BuildConfig

object AmplitudeWrapper {

    var amplitude: Amplitude? = null

    fun init(context: Context) {
        amplitude = Amplitude(
            Configuration(
                apiKey = BuildConfig.AMPLITUDE_API_KEY,
                context = context,
                defaultTracking = DefaultTrackingOptions(
                    sessions = true,
                    appLifecycles = true,
                )
            )
        )
    }
}