package ru.vitaliy.belyaev.wishapp.model.repository.analytics

import timber.log.Timber

class LogAnalyticsRepository : AnalyticsRepository {

    override fun trackEvent(eventName: String, block: ParametersBuilder.() -> Unit) {
        val parametersBuilder = ParametersBuilder()
        block.invoke(parametersBuilder)
        Timber.tag("Analytics").d("Track event:$eventName, params:${parametersBuilder.params}")
    }
}