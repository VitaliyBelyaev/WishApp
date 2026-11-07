package ru.vitaliy.belyaev.wishapp.shared.domain.entity

/**
 * Цвет ярлыка. В базе хранится [storedValue], тон по нему выбирает платформа.
 * Порядок записей — порядок слотов, так же раздаёт цвета миграция `3.sqm`.
 */
enum class TagColor(val storedValue: String) {
    BUTTER("butter"),
    MINT("mint"),
    SKY("sky"),
    LILAC("lilac"),
    ROSE("rose"),
    LIME("lime"),
    AQUA("aqua"),
    IRIS("iris"),
    ORCHID("orchid"),
    SAND("sand"),
    ;

    companion object {
        /** Неизвестное значение, например из новой версии в старой, читается как [BUTTER]. */
        fun fromStoredValue(value: String): TagColor = entries.firstOrNull { it.storedValue == value } ?: BUTTER

        /** Самый редкий цвет среди ярлыков, при равенстве первый по порядку. */
        fun leastUsed(countsByStoredValue: Map<String, Long>): TagColor =
            entries.minBy { countsByStoredValue[it.storedValue] ?: 0L }
    }
}

/** Ключ сравнения названий ярлыков: «Дом» и « дом » — один ярлык. */
fun normalizeTagTitle(title: String): String = title.trim().lowercase()
