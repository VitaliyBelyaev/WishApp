package ru.vitaliy.belyaev.wishapp.shared.data.database

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.inMemoryDriver

actual fun createTestDatabaseDriver(): SqlDriver = inMemoryDriver(WishAppDb.Schema)

actual fun createEmptyTestDriver(): SqlDriver = inMemoryDriver(EmptySchema)

private object EmptySchema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> = QueryResult.Unit

    override fun migrate(
        driver: SqlDriver,
        oldVersion: Long,
        newVersion: Long,
        vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
}
