package ru.vitaliy.belyaev.wishapp.data.repository.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmplitudeAnalyticsRepository @Inject constructor() : AnalyticsRepository {

    override fun trackEvent(eventName: String, block: ParametersBuilder.() -> Unit) {
        val parametersBuilder = ParametersBuilder()
        block.invoke(parametersBuilder)
        val params = parametersBuilder.params
        if (params.isEmpty()) {
            AmplitudeWrapper.amplitude?.track(eventName)
        } else {
            AmplitudeWrapper.amplitude?.track(eventName, eventProperties = params)
        }
    }
}