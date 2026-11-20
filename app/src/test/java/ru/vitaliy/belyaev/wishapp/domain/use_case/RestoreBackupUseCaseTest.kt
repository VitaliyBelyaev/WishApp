package ru.vitaliy.belyaev.wishapp.domain.use_case

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.domain.model.error.BackupFromNewerVersionException
import ru.vitaliy.belyaev.wishapp.domain.model.error.InvalidBackupFileException
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupLoadProgressListener
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository
import ru.vitaliy.belyaev.wishapp.shared.data.database.BackupFileCheck

/** Замена базы копией: пока копия не скачана и не проверена целиком, текущая база не меняется. */
class RestoreBackupUseCaseTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var dbFile: File
    private lateinit var journal: File
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var dataStoreRepository: DataStoreRepository

    @Before
    fun setUp() {
        dbFile = folder.newFile("wishapp.db").apply { writeBytes(OLD_DB) }
        journal = folder.newFile("wishapp.db-journal")
        dataStoreScope = CoroutineScope(UnconfinedTestDispatcher() + SupervisorJob())
        dataStoreRepository = DataStoreRepository(
            PreferenceDataStoreFactory.create(scope = dataStoreScope) { File(folder.root, "test.preferences_pb") },
        )
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun replacesDatabaseWithCheckedBackup() = runTest {
        val backup = sqliteBytes(BackupFileCheck.appSchemaVersion)

        useCase(download = { it.write(backup) }).restore()

        assertArrayEquals(backup, dbFile.readBytes())
        assertFalse(restoreFile().exists())
        assertFalse(journal.exists())
        assertTrue(dataStoreRepository.isRestoreFromBackupWasSucceed.first())
    }

    @Test
    fun keepsDatabaseWhenIntegrityCheckFails() = runTest {
        val useCase = useCase(download = { it.write(sqliteBytes(1)) }, isIntact = false)

        assertFailsWith<InvalidBackupFileException> { useCase.restore() }

        assertKeptOldDatabase()
    }

    @Test
    fun keepsDatabaseWhenFileIsNotDatabase() = runTest {
        val useCase = useCase(download = { it.write(ByteArray(4096) { 7 }) })

        assertFailsWith<InvalidBackupFileException> { useCase.restore() }

        assertKeptOldDatabase()
    }

    @Test
    fun keepsDatabaseWhenDownloadBreaks() = runTest {
        val useCase = useCase(
            download = {
                it.write(sqliteBytes(1).copyOf(PARTIAL_BYTES))
                throw IOException("connection reset")
            },
        )

        assertFailsWith<IOException> { useCase.restore() }

        assertKeptOldDatabase()
    }

    @Test
    fun keepsDatabaseWhenBackupIsFromNewerVersion() = runTest {
        val useCase = useCase(download = { it.write(sqliteBytes(BackupFileCheck.appSchemaVersion + 1)) })

        assertFailsWith<BackupFromNewerVersionException> { useCase.restore() }

        assertKeptOldDatabase()
    }

    private suspend fun assertKeptOldDatabase() {
        assertArrayEquals(OLD_DB, dbFile.readBytes())
        assertFalse(restoreFile().exists())
        assertTrue(journal.exists())
        assertFalse(dataStoreRepository.isRestoreFromBackupWasSucceed.first())
    }

    private fun restoreFile() = File(dbFile.path + RestoreBackupUseCase.RESTORE_SUFFIX)

    private fun useCase(download: (OutputStream) -> Unit, isIntact: Boolean = true) = RestoreBackupUseCase(
        backupRepository = FakeBackupRepository(download),
        dataStoreRepository = dataStoreRepository,
        backupFileVerifier = { isIntact },
    )

    private suspend fun RestoreBackupUseCase.restore() = invoke(
        backup = BackupInfo.Value(
            fileId = "file",
            modifiedDateTime = LocalDateTime.of(2026, 11, 20, 12, 0),
            sizeInBytes = 0,
            accountEmail = null,
            device = null,
        ),
        fileWhereCurrentDbStored = dbFile,
        progressListener = BackupLoadProgressListener { _, _ -> },
    )

    private inline fun <reified T : Throwable> assertFailsWith(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            assertEquals(T::class, e::class)
            return
        }
        fail("Expected ${T::class.simpleName}")
    }

    private class FakeBackupRepository(private val download: (OutputStream) -> Unit) : BackupRepository {

        override suspend fun checkExistingBackup(): BackupInfo = error("not used")

        override suspend fun uploadNewBackup(
            backupFile: File,
            progressListener: BackupLoadProgressListener,
        ): BackupInfo.Value = error("not used")

        override suspend fun downloadBackup(
            fileId: String,
            outputStream: OutputStream,
            progressListener: BackupLoadProgressListener,
        ) = download(outputStream)
    }

    private companion object {
        val OLD_DB = "old database".encodeToByteArray()
        const val PARTIAL_BYTES = 20

        /** Похоже на базу SQLite: сигнатура и `user_version` в заголовке, остальное нули. */
        fun sqliteBytes(schemaVersion: Long): ByteArray = ByteArray(4096).also { bytes ->
            "SQLite format 3\u0000".encodeToByteArray().copyInto(bytes)
            (0 until 4).forEach { i -> bytes[60 + i] = (schemaVersion shr (24 - 8 * i)).toByte() }
        }
    }
}
