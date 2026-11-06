package ru.vitaliy.belyaev.wishapp.domain.repository

import ru.vitaliy.belyaev.wishapp.domain.model.UiLookMode

interface RemoteConfigRepository {

    /**
     * Режим, активированный при прошлых запусках. Без сети это последнее полученное значение,
     * при самом первом запуске значение из кода.
     */
    fun activatedUiLookMode(): UiLookMode

    /**
     * Загружает и активирует новые значения в фоне. Текущий запуск их не использует:
     * вид меняется только со следующего запуска, чтобы интерфейс не менялся на глазах.
     */
    fun fetchForNextLaunch()
}
