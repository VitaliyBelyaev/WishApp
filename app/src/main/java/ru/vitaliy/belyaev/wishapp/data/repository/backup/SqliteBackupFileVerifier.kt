package ru.vitaliy.belyaev.wishapp.data.repository.backup

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import java.io.File
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupFileVerifier
import timber.log.Timber

/**
 * Открывает копию только на чтение и делает `PRAGMA quick_check`: обрезанный или битый файл
 * не должен заменить рабочую базу.
 */
internal class SqliteBackupFileVerifier : BackupFileVerifier {

    // Обработчик по умолчанию при порче удаляет файл. Здесь порча — просто ответ «нет», файл удалит вызывающий
    override fun isIntactDatabase(file: File): Boolean = try {
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY) { }.use { db ->
            db.rawQuery("PRAGMA quick_check", null).use { cursor ->
                cursor.moveToFirst() && cursor.getString(0) == QUICK_CHECK_OK
            }
        }
    } catch (e: SQLiteException) {
        Timber.w(e, "Backup file is not an intact SQLite database")
        false
    }

    private companion object {
        const val QUICK_CHECK_OK = "ok"
    }
}
