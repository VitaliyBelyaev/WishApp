package ru.vitaliy.belyaev.wishapp.shared.data.database

import android.content.Context
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import ru.vitaliy.belyaev.wishapp.shared.data.Config

actual class DatabaseDriverFactory(private val context: Context) {

    actual fun createDatabaseDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = WishAppDb.Schema,
            context = context,
            name = Config.DATABASE_NAME,
            callback = AndroidSqliteDriver.Callback(
                schema = WishAppDb.Schema,
                AfterVersion(1) { DatabaseMigrator.updateWishPositionsAfterMigrationFrom1To2(it) },
            )
        )
    }
}