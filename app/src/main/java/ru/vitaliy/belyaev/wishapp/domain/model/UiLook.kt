package ru.vitaliy.belyaev.wishapp.domain.model

/** Какой вид приложения показывать: прежний или новый после редизайна. */
enum class UiLook {
    OLD, NEW;

    companion object {
        fun fromStoredValue(value: String?): UiLook? = entries.firstOrNull { it.storedValue == value }
    }
}

val UiLook.storedValue: String
    get() = when (this) {
        UiLook.OLD -> "old"
        UiLook.NEW -> "new"
    }

/**
 * Режим перехода на новый вид, флаг `ui_look_mode` в Remote Config.
 *
 * - [OLD_ONLY]: новый вид не виден, режим на время разработки.
 * - [CHOICE]: все на новом, выбор в настройках.
 * - [NEW_ONLY]: выбора нет.
 */
enum class UiLookMode {
    OLD_ONLY, CHOICE, NEW_ONLY;

    companion object {
        /** Неизвестное или пустое значение из Remote Config считается [OLD_ONLY]. */
        fun fromRemoteValue(value: String?): UiLookMode = when (value?.trim()?.lowercase()) {
            "choice" -> CHOICE
            "new_only" -> NEW_ONLY
            else -> OLD_ONLY
        }
    }
}

/**
 * Вид, который увидит пользователь.
 *
 * Локальный выбор не стирается при смене режима, поэтому с `new_only` можно откатиться на `choice`
 * и пользователь вернётся к своему выбору.
 *
 * @param allowChoiceInOldOnly выбор работает и при [UiLookMode.OLD_ONLY] и до выпуска нового вида:
 * так в отладочной сборке новый вид открывается во время разработки.
 * @param isNewLookReleased новый вид готов. До этого показываем прежний при любом режиме,
 * чтобы случайно включённый флаг не открыл недоделанный экран.
 */
fun resolveUiLook(
    mode: UiLookMode,
    userChoice: UiLook?,
    allowChoiceInOldOnly: Boolean,
    isNewLookReleased: Boolean,
): UiLook {
    if (allowChoiceInOldOnly && (mode == UiLookMode.OLD_ONLY || !isNewLookReleased)) {
        return userChoice ?: UiLook.OLD
    }
    if (!isNewLookReleased) return UiLook.OLD
    return when (mode) {
        UiLookMode.OLD_ONLY -> UiLook.OLD
        UiLookMode.CHOICE -> userChoice ?: UiLook.NEW
        UiLookMode.NEW_ONLY -> UiLook.NEW
    }
}
