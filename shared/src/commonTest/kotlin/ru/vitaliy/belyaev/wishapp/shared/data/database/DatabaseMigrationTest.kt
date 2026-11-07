package ru.vitaliy.belyaev.wishapp.shared.data.database

import app.cash.sqldelight.db.SqlDriver
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseMigrationTest {

    private val driver: SqlDriver = createEmptyTestDriver()

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    @Test
    fun migrationFrom3_setsTagColorsByRowid() {
        createSchemaVersion3()
        (1..11).forEach { driver.exec("INSERT INTO Tag(tagId, title) VALUES ('t$it', 'Ярлык $it')") }

        migrateFrom3()

        val colors = driver.queryRows("SELECT tagId, color FROM Tag ORDER BY rowid", columns = 2)
            .associate { it[0] to it[1] }
        assertEquals("mint", colors["t1"])
        assertEquals("sky", colors["t2"])
        assertEquals("sand", colors["t9"])
        assertEquals("butter", colors["t10"])
        assertEquals("mint", colors["t11"])
    }

    @Test
    fun migrationFrom3_keepsPhotoOrderInsideEachWish() {
        createSchemaVersion3()
        driver.exec("INSERT INTO Image(id, wishId, rawData) VALUES ('a', 'w1', x'00')")
        driver.exec("INSERT INTO Image(id, wishId, rawData) VALUES ('b', 'w2', x'00')")
        driver.exec("INSERT INTO Image(id, wishId, rawData) VALUES ('c', 'w1', x'00')")
        driver.exec("INSERT INTO Image(id, wishId, rawData) VALUES ('d', 'w1', x'00')")

        migrateFrom3()

        val positions = driver.queryRows("SELECT id, position FROM Image", columns = 2)
            .associate { it[0] to it[1] }
        assertEquals(mapOf<String?, String?>("a" to "0", "b" to "0", "c" to "1", "d" to "2"), positions)
    }

    @Test
    fun migrationFrom3_leavesCompletionDateEmptyAndNoTagShown() {
        createSchemaVersion3()
        insertWishV3("w1", isCompleted = true, position = -1)
        driver.exec("INSERT INTO Tag(tagId, title) VALUES ('t1', 'Дом')")
        driver.exec("INSERT INTO WishTagRelation(wishId, tagId) VALUES ('w1', 't1')")

        migrateFrom3()

        assertEquals(listOf(listOf<String?>(null)), driver.queryRows("SELECT completedTimestamp FROM Wish", 1))
        assertEquals(listOf(listOf<String?>("0")), driver.queryRows("SELECT isShown FROM WishTagRelation", 1))
    }

    @Test
    fun migrationFrom3_mergesTagsDifferingByCaseAndSpaces_keepsTagWithMoreWishes() {
        createSchemaVersion3()
        listOf("w1", "w2", "w3").forEachIndexed { index, id -> insertWishV3(id, isCompleted = false, position = index) }
        driver.exec("INSERT INTO Tag(tagId, title) VALUES ('home1', 'Дом')")
        driver.exec("INSERT INTO Tag(tagId, title) VALUES ('home2', 'дом ')")
        driver.exec("INSERT INTO Tag(tagId, title) VALUES ('home3', 'ДОМ')")
        driver.exec("INSERT INTO Tag(tagId, title) VALUES ('dacha', 'Дача')")
        driver.exec("INSERT INTO WishTagRelation(wishId, tagId) VALUES ('w1', 'home1')")
        driver.exec("INSERT INTO WishTagRelation(wishId, tagId) VALUES ('w1', 'home2')")
        driver.exec("INSERT INTO WishTagRelation(wishId, tagId) VALUES ('w2', 'home2')")
        driver.exec("INSERT INTO WishTagRelation(wishId, tagId) VALUES ('w3', 'home3')")
        driver.exec("INSERT INTO WishTagRelation(wishId, tagId) VALUES ('w3', 'dacha')")

        migrateFrom3()

        assertEquals(
            listOf(listOf<String?>("dacha", "Дача"), listOf<String?>("home2", "дом ")),
            driver.queryRows("SELECT tagId, title FROM Tag ORDER BY tagId", columns = 2),
        )
        assertEquals(
            listOf(
                listOf<String?>("w1", "home2"),
                listOf<String?>("w2", "home2"),
                listOf<String?>("w3", "dacha"),
                listOf<String?>("w3", "home2"),
            ),
            driver.queryRows("SELECT wishId, tagId FROM WishTagRelation ORDER BY wishId, tagId", columns = 2),
        )
    }

    @Test
    fun migrationFrom3_mergesTagsWithEqualWishCount_keepsEarlierTag() {
        createSchemaVersion3()
        listOf("w1", "w2").forEachIndexed { index, id -> insertWishV3(id, isCompleted = false, position = index) }
        driver.exec("INSERT INTO Tag(tagId, title) VALUES ('older', 'Сад')")
        driver.exec("INSERT INTO Tag(tagId, title) VALUES ('newer', 'сад')")
        driver.exec("INSERT INTO WishTagRelation(wishId, tagId) VALUES ('w1', 'older')")
        driver.exec("INSERT INTO WishTagRelation(wishId, tagId) VALUES ('w2', 'newer')")

        migrateFrom3()

        assertEquals(listOf(listOf<String?>("older")), driver.queryRows("SELECT tagId FROM Tag", columns = 1))
        assertEquals(
            listOf(listOf<String?>("w1", "older"), listOf<String?>("w2", "older")),
            driver.queryRows("SELECT wishId, tagId FROM WishTagRelation ORDER BY wishId", columns = 2),
        )
    }

    @Test
    fun migrationFrom1_setsPositionsOfActiveWishesInStoredOrder() {
        driver.exec(
            """
            CREATE TABLE Wish (
              wishId TEXT PRIMARY KEY NOT NULL,
              title TEXT NOT NULL,
              link TEXT NOT NULL,
              comment TEXT NOT NULL,
              isCompleted INTEGER DEFAULT 0 NOT NULL,
              createdTimestamp INTEGER NOT NULL,
              updatedTimestamp INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        driver.exec("CREATE TABLE Tag (tagId TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL)")
        driver.exec(CREATE_RELATION_V3)
        driver.exec("INSERT INTO Wish VALUES ('w1', 'Первое', '', '', 0, 1, 1)")
        driver.exec("INSERT INTO Wish VALUES ('w2', 'Сбылось', '', '', 1, 2, 2)")
        driver.exec("INSERT INTO Wish VALUES ('w3', 'Третье', '', '', 0, 3, 3)")

        WishAppDb.Schema.migrate(
            driver,
            oldVersion = 1,
            newVersion = WishAppDb.Schema.version,
            *DatabaseMigrator.afterVersionCallbacks,
        )

        assertEquals(
            listOf(listOf<String?>("w1", "0"), listOf<String?>("w2", "-1"), listOf<String?>("w3", "1")),
            driver.queryRows("SELECT wishId, position FROM Wish ORDER BY wishId", columns = 2),
        )
    }

    private fun migrateFrom3() {
        WishAppDb.Schema.migrate(
            driver,
            oldVersion = 3,
            newVersion = WishAppDb.Schema.version,
            *DatabaseMigrator.afterVersionCallbacks,
        )
    }

    private fun insertWishV3(id: String, isCompleted: Boolean, position: Int) {
        val completed = if (isCompleted) 1 else 0
        driver.exec("INSERT INTO Wish VALUES ('$id', '$id', '', '', $completed, 1, 1, $position)")
    }

    private fun createSchemaVersion3() {
        driver.exec(
            """
            CREATE TABLE Wish (
              wishId TEXT PRIMARY KEY NOT NULL,
              title TEXT NOT NULL,
              link TEXT NOT NULL,
              comment TEXT NOT NULL,
              isCompleted INTEGER DEFAULT 0 NOT NULL,
              createdTimestamp INTEGER NOT NULL,
              updatedTimestamp INTEGER NOT NULL,
              position INTEGER DEFAULT -1 NOT NULL
            )
            """.trimIndent(),
        )
        driver.exec("CREATE TABLE Image (id TEXT PRIMARY KEY NOT NULL, wishId TEXT NOT NULL, rawData BLOB NOT NULL)")
        driver.exec("CREATE TABLE Tag (tagId TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL)")
        driver.exec(CREATE_RELATION_V3)
    }

    private companion object {
        const val CREATE_RELATION_V3 = """
            CREATE TABLE WishTagRelation (
             wishId TEXT NOT NULL,
             tagId TEXT NOT NULL,
             FOREIGN KEY (wishId) REFERENCES Wish(wishId),
             FOREIGN KEY (tagId) REFERENCES Tag(tagId),
             UNIQUE(wishId, tagId)
            )
        """
    }
}
