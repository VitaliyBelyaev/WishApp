package ru.vitaliy.belyaev.wishapp.shared.data.database

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration
import ru.vitaliy.belyaev.wishapp.shared.data.Config

actual class DatabaseDriverFactory {

    actual fun createDatabaseDriver(): SqlDriver {
        return NativeSqliteDriver(
            configuration = DatabaseConfiguration(
                name = Config.DATABASE_NAME,
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
                            AfterVersion(1) { DatabaseMigrator.updateWishPositionsAfterMigrationFrom1To2(it) }
                        )
                    }
                }
            )
        )
    }
}