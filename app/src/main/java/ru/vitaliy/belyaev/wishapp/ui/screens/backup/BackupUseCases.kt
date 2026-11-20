package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.domain.use_case.CreateBackupUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.GetExistingBackupInfoUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.IsUserSignedInToBackupServiceUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.NeedToShowRestoreUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.RestoreBackupUseCase
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk

/** Use case'ы копии, общие с прежним видом, и файл базы, который копируется и заменяется. */
internal class BackupUseCases @Inject constructor(
    private val database: BackupDatabaseFile,
    val isSignedIn: IsUserSignedInToBackupServiceUseCase,
    val getExistingBackupInfo: GetExistingBackupInfoUseCase,
    val needToShowRestore: NeedToShowRestoreUseCase,
    val createBackup: CreateBackupUseCase,
    val restoreBackup: RestoreBackupUseCase,
) {

    val databaseFile: File
        get() = database.file
}

/** Файл базы приложения: его копия уходит в Drive, на его место встаёт восстановленная. */
internal class BackupDatabaseFile @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wishAppSdk: WishAppSdk,
) {

    val file: File
        get() = context.getDatabasePath(wishAppSdk.databaseName)
}
