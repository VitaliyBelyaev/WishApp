package ru.vitaliy.belyaev.wishapp.domain.repository

import java.io.File

/** Проверка скачанной копии до того, как она заменит базу. */
internal fun interface BackupFileVerifier {

    /** Файл открывается как база SQLite и проходит проверку целостности. Файл не меняется. */
    fun isIntactDatabase(file: File): Boolean
}
