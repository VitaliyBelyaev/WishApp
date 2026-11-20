package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import androidx.annotation.StringRes
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo

/**
 * Что показывает экран копии. [status] — карточка сверху, [work] — идёт сохранение или
 * восстановление. Пока идёт работа, другие действия недоступны. [accountPhotoUrl] и
 * [accountName] — фото и имя из профиля Google, если они есть.
 */
internal data class BackupState(
    val status: BackupStatus = BackupStatus.Loading,
    val work: BackupWork? = null,
    val accountPhotoUrl: String? = null,
    val accountName: String? = null,
)

internal sealed interface BackupStatus {

    /** Первый миг: ещё не знаем, вошли ли в аккаунт. Карточки нет. */
    data object Loading : BackupStatus

    /** Не вошли или доступ к Drive отозван: просим разрешение. */
    data object SignedOut : BackupStatus

    data class Checking(val account: String?) : BackupStatus

    data class CheckFailed(val account: String?) : BackupStatus

    data class NoBackup(val account: String?) : BackupStatus

    /**
     * Копия есть. [isRestoreSuggested] — на этом телефоне её ещё не восстанавливали и отсюда копию
     * не создавали: главное действие «Вернуть данные», новая копия только с предупреждением.
     */
    data class HasBackup(val backup: BackupInfo.Value, val isRestoreSuggested: Boolean) : BackupStatus
}

internal val BackupStatus.account: String?
    get() = when (this) {
        is BackupStatus.Checking -> account
        is BackupStatus.CheckFailed -> account
        is BackupStatus.NoBackup -> account
        is BackupStatus.HasBackup -> backup.accountEmail
        BackupStatus.Loading, BackupStatus.SignedOut -> null
    }

internal sealed interface BackupWork {

    /** Сколько загружено, `null`, пока размер неизвестен. */
    val progress: BackupProgress?

    data class Saving(override val progress: BackupProgress? = null) : BackupWork

    data class Restoring(override val progress: BackupProgress? = null) : BackupWork
}

/** [fraction] от 0 до 1. */
internal data class BackupProgress(val fraction: Float, val loadedBytes: Long, val totalBytes: Long)

/** Разовые события экрана: сообщения и перезапуск после восстановления. */
internal sealed interface BackupEvent {

    /** Снэкбар. С [retry] у него кнопка «Повторить». */
    data class Message(@StringRes val text: Int, val retry: (() -> Unit)? = null) : BackupEvent

    /** Копия из более новой версии: снэкбар с «Обновить», висит, пока его не закроют. */
    data object NewerVersion : BackupEvent

    /** Данные заменены: сообщение и перезапуск приложения. */
    data object Restored : BackupEvent
}
