package ru.vitaliy.belyaev.wishapp.model.repository.analytics

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsRepository : AnalyticsRepository {

    override fun trackEvent(eventName: String, block: ParametersBuilder.() -> Unit) {
        val parametersBuilder = ParametersBuilder()
        block.invoke(parametersBuilder)
        Firebase.analytics.logEvent(eventName) {
            val params = parametersBuilder.params
            if (params.isNotEmpty()) {
                val iterator = params.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    param(entry.key, entry.value)
                }
            }
        }
    }
}