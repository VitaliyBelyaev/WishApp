package ru.vitaliy.belyaev.wishapp.domain.model

/** Вид списка в новом виде. Значения хранятся в DataStore: не менять и не переиспользовать. */
enum class ListLayout(val storedValue: String) {
    CARDS("cards"),
    LIST("list");

    companion object {
        fun fromStoredValue(value: String?): ListLayout = entries.firstOrNull { it.storedValue == value } ?: CARDS
    }
}

/**
 * Сортировка «Сбылось» в новом виде. «Моего порядка» нет: у сбывшихся нет позиции. Запоминается отдельно
 * от основного списка. Значения хранятся в DataStore: не менять и не переиспользовать.
 */
enum class DoneSortMode(val storedValue: String) {
    /** По дате исполнения, по умолчанию. */
    NEWEST("newest"),
    OLDEST("oldest"),
    TITLE_AZ("title_az"),
    TITLE_ZA("title_za");

    companion object {
        val DEFAULT = NEWEST

        fun fromStoredValue(value: String?): DoneSortMode = entries.firstOrNull { it.storedValue == value } ?: DEFAULT
    }
}
