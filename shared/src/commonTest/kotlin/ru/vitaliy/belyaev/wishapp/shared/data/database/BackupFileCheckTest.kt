package ru.vitaliy.belyaev.wishapp.shared.data.database

import kotlin.test.Test
import kotlin.test.assertEquals

class BackupFileCheckTest {

    @Test
    fun backupOfSameSchema_isCompatible() {
        assertEquals(BackupFileCheckResult.Compatible, BackupFileCheck.check(header(userVersion = 4), appSchemaVersion = 4))
    }

    @Test
    fun backupOfOlderSchema_isCompatible_migrationsUpgradeIt() {
        assertEquals(BackupFileCheckResult.Compatible, BackupFileCheck.check(header(userVersion = 1), appSchemaVersion = 4))
    }

    @Test
    fun backupOfNewerSchema_isRejected() {
        assertEquals(
            BackupFileCheckResult.FromNewerVersion(backupSchemaVersion = 5, appSchemaVersion = 4),
            BackupFileCheck.check(header(userVersion = 5), appSchemaVersion = 4),
        )
    }

    @Test
    fun userVersionIsReadAsBigEndian() {
        assertEquals(
            BackupFileCheckResult.FromNewerVersion(backupSchemaVersion = 0x0102, appSchemaVersion = 4),
            BackupFileCheck.check(header(userVersion = 0x0102), appSchemaVersion = 4),
        )
    }

    @Test
    fun fileWithoutSqliteHeader_isNotDatabase() {
        val bytes = ByteArray(200) { 'x'.code.toByte() }
        assertEquals(BackupFileCheckResult.NotDatabase, BackupFileCheck.check(bytes, appSchemaVersion = 4))
    }

    @Test
    fun truncatedFile_isNotDatabase() {
        assertEquals(BackupFileCheckResult.NotDatabase, BackupFileCheck.check(header(4).copyOf(50), appSchemaVersion = 4))
    }

    @Test
    fun defaultAppVersion_isCurrentSchema() {
        val current = WishAppDb.Schema.version
        assertEquals(BackupFileCheckResult.Compatible, BackupFileCheck.check(header(current)))
        assertEquals(
            BackupFileCheckResult.FromNewerVersion(current + 1, current),
            BackupFileCheck.check(header(current + 1)),
        )
    }

    @Test
    fun knownSchemaVersionIsCheckedWithoutFile() {
        val current = BackupFileCheck.appSchemaVersion
        assertEquals(WishAppDb.Schema.version, current)
        assertEquals(BackupFileCheckResult.Compatible, BackupFileCheck.checkSchemaVersion(current))
        assertEquals(BackupFileCheckResult.Compatible, BackupFileCheck.checkSchemaVersion(current - 1))
        assertEquals(
            BackupFileCheckResult.FromNewerVersion(current + 1, current),
            BackupFileCheck.checkSchemaVersion(current + 1),
        )
    }

    @Test
    fun rememberedRefusalPassesAfterAppCaughtUp() {
        // Копия со схемой 4 в приложении со схемой 3 отклоняется, после обновления до 4 проходит
        assertEquals(
            BackupFileCheckResult.FromNewerVersion(4, 3),
            BackupFileCheck.checkSchemaVersion(backupSchemaVersion = 4, appSchemaVersion = 3),
        )
        assertEquals(
            BackupFileCheckResult.Compatible,
            BackupFileCheck.checkSchemaVersion(backupSchemaVersion = 4, appSchemaVersion = 4),
        )
    }

    private fun header(userVersion: Long): ByteArray {
        val bytes = ByteArray(4096)
        "SQLite format 3\u0000".encodeToByteArray().copyInto(bytes)
        for (i in 0 until 4) bytes[60 + i] = (userVersion shr (8 * (3 - i))).toByte()
        return bytes
    }
}
