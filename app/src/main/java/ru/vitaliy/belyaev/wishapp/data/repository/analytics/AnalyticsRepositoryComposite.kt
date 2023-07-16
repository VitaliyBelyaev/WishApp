package ru.vitaliy.belyaev.wishapp.data.repository.analytics

class AnalyticsRepositoryComposite(
    private val repositories: List<AnalyticsRepository>
) : AnalyticsRepository {

    override fun trackEvent(eventName: String, block: ParametersBuilder.() -> Unit) {
        repositories.forEach { it.trackEvent(eventName, block) }
    }
}