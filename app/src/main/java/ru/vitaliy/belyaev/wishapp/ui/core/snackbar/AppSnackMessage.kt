package ru.vitaliy.belyaev.wishapp.ui.core.snackbar

/**
 * Сообщение для единственного снэкбара приложения, хост которого лежит над навигацией.
 *
 * С [actionLabel] это снэкбар с отменой: [onAction] вызывается по кнопке, [onDismiss] когда снэкбар
 * исчез сам или его заменило новое сообщение. Тогда прошлое действие считается подтверждённым,
 * например удаление выполняется в [onDismiss].
 *
 * Прежний вид показывает только [text]: сообщения с действием отправляет только новый вид.
 */
class AppSnackMessage(
    val text: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val onDismiss: (() -> Unit)? = null,
)
