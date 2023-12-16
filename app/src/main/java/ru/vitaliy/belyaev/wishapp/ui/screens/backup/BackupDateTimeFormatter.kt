package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object BackupDateTimeFormatter {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")

    fun formatBackupDateTime(localDateTime: LocalDateTime): String {
        return dateTimeFormatter.format(localDateTime)
    }
}