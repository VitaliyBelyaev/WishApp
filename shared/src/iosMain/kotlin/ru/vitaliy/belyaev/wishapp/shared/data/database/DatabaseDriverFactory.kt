package ru.vitaliy.belyaev.wishapp.shared.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration

actual class DatabaseDriverFactory {

    actual fun createDatabaseDriver(databaseName: String): SqlDriver {
        return NativeSqliteDriver(
            configuration = DatabaseConfiguration(
                name = databaseName,
                version = WishAppDb.Schema.version.toInt(),
                create = { connection ->
                    wrapConnection(connection) { WishAppDb.Schema.create(it) }
                },
                upgrade = { connection, oldVersion, newVersion ->
                    wrapConnection(connection) { driver ->
                        WishAppDb.Schema.migrate(
                            driver = driver,
                            oldVersion = oldVersion.toLong(),
                            newVersion = newVersion.toLong(),
                            *DatabaseMigrator.afterVersionCallbacks,
                        )
                    }
                },
            ),
        )
    }
}
