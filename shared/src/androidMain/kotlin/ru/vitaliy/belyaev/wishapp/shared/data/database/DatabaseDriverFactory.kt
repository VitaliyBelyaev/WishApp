package ru.vitaliy.belyaev.wishapp.shared.data.database

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.AfterVersionWithDriver
import com.squareup.sqldelight.db.SqlDriver
import ru.vitaliy.belyaev.wishapp.shared.data.Config

actual class DatabaseDriverFactory(private val context: Context) {

    actual fun createDatabaseDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = WishAppDb.Schema,
            context = context,
            name = Config.DATABASE_NAME,
            callback = AndroidSqliteDriver.Callback(
                schema = WishAppDb.Schema,
                AfterVersionWithDriver(1) { DatabaseMigrator.updateWishPositionsAfterMigrationFrom1To2(it) },
            )
        )
    }
}