package ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events

import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent

object EditTagsRenameTagDoneClickedEvent : AnalyticsEvent {

    override val name: String = "Edit Tags Screen - Rename Tag Done Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object EditTagsDeleteTagConfirmedEvent : AnalyticsEvent {

    override val name: String = "Edit Tags Screen - Delete Tag Confirmed"

    override val params: Map<String, Any?> = emptyMap()
}

/** Цвет ярлыка сменили вручную на экране управления. [tone] — слот цвета, как он хранится в базе. */
data class EditTagsTagColorChangedEvent(val tone: String) : AnalyticsEvent {

    override val name: String = "Edit Tags Screen - Tag Color Changed"

    override val params: Map<String, Any?> = mapOf("tone" to tone)
}

/** Ярлык создан из поиска экрана управления: строкой «Создать» или «Готово» на клавиатуре. */
object EditTagsTagCreatedEvent : AnalyticsEvent {

    override val name: String = "Edit Tags Screen - Tag Created"

    override val params: Map<String, Any?> = emptyMap()
}
