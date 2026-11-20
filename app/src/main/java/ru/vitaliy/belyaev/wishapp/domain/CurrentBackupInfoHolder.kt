package ru.vitaliy.belyaev.wishapp.domain

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo

/**
 * Что известно о копии, пока жив процесс. Экран копии показывает это сразу, без запроса в Drive:
 * проверить заново можно кнопкой «Обновить».
 */
@Singleton
internal class CurrentBackupInfoHolder @Inject constructor() {

    private val _backupInfo: MutableStateFlow<BackupInfo> = MutableStateFlow(BackupInfo.None())
    val backupInfo: StateFlow<BackupInfo> = _backupInfo.asStateFlow()

    /** Копию в этом запуске уже проверяли: [backupInfo] — ответ, в том числе «копии нет». */
    @Volatile
    var isKnown: Boolean = false
        private set

    fun updateBackupInfo(backupInfo: BackupInfo) {
        _backupInfo.value = backupInfo
        isKnown = true
    }

    /** Вышли из аккаунта или доступ отозван: о копии снова ничего не известно. */
    fun clear() {
        _backupInfo.value = BackupInfo.None()
        isKnown = false
    }
}
