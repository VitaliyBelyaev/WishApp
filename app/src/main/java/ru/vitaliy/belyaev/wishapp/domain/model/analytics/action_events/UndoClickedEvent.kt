package ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events

import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent

/**
 * «Вернуть» в снэкбаре нового вида: ошибаются ли без диалогов. Одно событие на все экраны,
 * что отменили — в `action`. Остальные действия (`delete_wish`, `done`, `delete_link`,
 * `delete_image`) добавятся вместе с аналитикой своих экранов.
 */
data class UndoClickedEvent(val action: UndoAction) : AnalyticsEvent {

    override val name: String = "Undo Clicked"

    override val params: Map<String, Any?> = mapOf("action" to action.value)
}

enum class UndoAction(val value: String) {
    DELETE_TAG("delete_tag"),
}
