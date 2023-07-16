package ru.vitaliy.belyaev.wishapp.entity.analytics.action_events

import ru.vitaliy.belyaev.wishapp.entity.analytics.AnalyticsEvent

data class SettingsSelectAppThemeClickedEvent(
    val theme: String
) : AnalyticsEvent {

    override val name: String = "Settings Screen - Select App Theme Clicked"

    override val params: Map<String, Any?> = mapOf(
        "theme" to theme
    )
}

object SettingDataBackupClickedEvent : AnalyticsEvent {

    override val name: String = "Settings Screen - Data Backup Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object SettingRateAppClickedEvent : AnalyticsEvent {

    override val name: String = "Settings Screen - Rate App Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object SettingShareAppClickedEvent : AnalyticsEvent {

    override val name: String = "Settings Screen - Share App Clicked"

    override val params: Map<String, Any?> = emptyMap()
}