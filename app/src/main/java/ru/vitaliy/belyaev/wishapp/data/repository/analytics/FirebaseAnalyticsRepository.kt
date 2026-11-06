package ru.vitaliy.belyaev.wishapp.data.repository.analytics

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.crashlytics
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository

@Singleton
class FirebaseAnalyticsRepository : AnalyticsRepository {

    override fun trackEvent(event: AnalyticsEvent) {
        Firebase.analytics.logEvent(FirebaseEventNames.toFirebaseName(event.name)) {
            event.params.entries.forEach { paramEntry ->
                when (val paramValue = paramEntry.value) {
                    is Double -> param(paramEntry.key, paramValue)
                    is Long -> param(paramEntry.key, paramValue)
                    else -> param(paramEntry.key, paramValue.toString())
                }
            }
        }
    }

    /**
     * В Firebase Analytics свойства уходят строкой (имя до 24 символов, значение до 36).
     * Те же значения пишем ключами Crashlytics, чтобы делить падения по ним.
     */
    override fun setUserProperties(properties: Map<String, Any>) {
        properties.forEach { (key, value) ->
            Firebase.analytics.setUserProperty(key, value.toString())
            when (value) {
                is Boolean -> Firebase.crashlytics.setCustomKey(key, value)
                is Int -> Firebase.crashlytics.setCustomKey(key, value)
                is Long -> Firebase.crashlytics.setCustomKey(key, value)
                else -> Firebase.crashlytics.setCustomKey(key, value.toString())
            }
        }
    }
}
