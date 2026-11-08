package ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events

import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent

object InAppReviewRequestedEvent : AnalyticsEvent {

    override val name: String = "InApp Review Requested"

    override val params: Map<String, Any?> = emptyMap()
}

object InAppReviewShowEvent : AnalyticsEvent {

    override val name: String = "InApp Review Show"

    override val params: Map<String, Any?> = emptyMap()
}
/** Предложили обновить приложение через Google Play, гибкий режим. */
data class InAppUpdateShownEvent(
    val stalenessDays: Int,
    val availableVersionCode: Int,
) : AnalyticsEvent {

    override val name: String = "InApp Update Shown"

    override val params: Map<String, Any?> = mapOf(
        "staleness_days" to stalenessDays,
        "available_version_code" to availableVersionCode,
    )
}

/** Ответ на окно обновления Google Play: согласился скачать или отказался. */
data class InAppUpdateAnsweredEvent(val accepted: Boolean) : AnalyticsEvent {

    override val name: String = "InApp Update Answered"

    override val params: Map<String, Any?> = mapOf("accepted" to accepted)
}

object InAppUpdateDownloadedEvent : AnalyticsEvent {

    override val name: String = "InApp Update Downloaded"

    override val params: Map<String, Any?> = emptyMap()
}

object InAppUpdateRestartClickedEvent : AnalyticsEvent {

    override val name: String = "InApp Update Restart Clicked"

    override val params: Map<String, Any?> = emptyMap()
}
