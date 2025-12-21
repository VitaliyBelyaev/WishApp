package ru.vitaliy.belyaev.wishapp.data.repository.analytics

import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.Firebase
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent

@Singleton
class FirebaseAnalyticsRepository : AnalyticsRepository {

    override fun trackEvent(event: AnalyticsEvent) {
        Firebase.analytics.logEvent(event.name) {
            event.params.entries.forEach { paramEntry ->
                when (val paramValue = paramEntry.value) {
                    is Double -> param(paramEntry.key, paramValue)
                    is Long -> param(paramEntry.key, paramValue)
                    else -> param(paramEntry.key, paramValue.toString())
                }
            }
        }
    }
}