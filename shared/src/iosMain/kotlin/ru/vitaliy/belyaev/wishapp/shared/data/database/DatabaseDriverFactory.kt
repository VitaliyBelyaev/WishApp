package ru.vitaliy.belyaev.wishapp.shared.data.database

import co.touchlab.sqliter.DatabaseConfiguration
import com.squareup.sqldelight.db.AfterVersionWithDriver
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.migrateWithCallbacks
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import com.squareup.sqldelight.drivers.native.wrapConnection
import ru.vitaliy.belyaev.wishapp.shared.data.Config

actual class DatabaseDriverFactory {

    actual fun createDatabaseDriver(): SqlDriver {
        return NativeSqliteDriver(
            configuration = DatabaseConfiguration(
                name = Config.DATABASE_NAME,
                version = WishAppDb.Schema.version,
                create = { connection ->
                    wrapConnection(connection) { WishAppDb.Schema.create(it) }
                },
                upgrade = { connection, oldVersion, newVersion ->
                    wrapConnection(connection) { driver ->
                        WishAppDb.Schema.migrateWithCallbacks(
                            driver = driver,
                            oldVersion = oldVersion,
                            newVersion = newVersion,
                            AfterVersionWithDriver(1) { DatabaseMigrator.updateWishPositionsAfterMigrationFrom1To2(it) }
                        )
                    }
                }
            )
        )
    }
}