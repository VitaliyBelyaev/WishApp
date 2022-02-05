package ru.vitaliy.belyaev.wishapp.data.repository.analytics

interface AnalyticsRepository {

    fun trackEvent(eventName: String, block: ParametersBuilder.() -> Unit = {})
}

class ParametersBuilder {

    val params: MutableMap<String, String> = mutableMapOf()

    fun param(key: String, value: String) {
        params[key] = value
    }
}