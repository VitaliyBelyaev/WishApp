package ru.vitaliy.belyaev.wishapp.di

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.AfterVersionWithDriver
import com.squareup.sqldelight.db.SqlDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.vitaliy.belyaev.wishapp.data.database.Config
import ru.vitaliy.belyaev.wishapp.data.database.Wish
import ru.vitaliy.belyaev.wishapp.data.database.WishAppDb

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): WishAppDb {
        val sqlDriver = AndroidSqliteDriver(
            schema = WishAppDb.Schema,
            context = appContext,
            name = Config.DATABASE_NAME,
            callback = AndroidSqliteDriver.Callback(
                schema = WishAppDb.Schema,
                /**
                 * afterVersion is version of .sqm file. If we passed 1(for 1.sqm where we have migration
                 * statements for migration from version 1 to version 2 of database) that means that code
                 * block in AfterVersionWithDriver will be executed after 1.sqm migration from 1 to 2 has completed.
                 */
                AfterVersionWithDriver(afterVersion = 1) { updateWishPositionsAfterMigrationFrom1To2(it) }
            )
        )

        return WishAppDb(sqlDriver)
    }

    private fun updateWishPositionsAfterMigrationFrom1To2(sqlDriver: SqlDriver) {
        val sqlCursor = sqlDriver.executeQuery(null, "SELECT * FROM Wish", 0)
        val allWishesInDbOrder = mutableListOf<Wish>()
        sqlCursor.use { cursor ->
            while (cursor.next()) {
                val wish = Wish(
                    wishId = cursor.getString(0)!!,
                    title = cursor.getString(1)!!,
                    link = cursor.getString(2)!!,
                    comment = cursor.getString(3)!!,
                    isCompleted = cursor.getLong(4)!! == 1L,
                    createdTimestamp = cursor.getLong(5)!!,
                    updatedTimestamp = cursor.getLong(6)!!,
                    position = -1
                )
                allWishesInDbOrder.add(wish)
            }
        }
        allWishesInDbOrder
            .filter { !it.isCompleted }
            .forEachIndexed { index, wish ->
                sqlDriver.execute(
                    identifier = null,
                    sql = "UPDATE Wish SET position = ? WHERE  wishId = ?",
                    parameters = 2
                ) {
                    bindLong(1, index.toLong())
                    bindString(2, wish.wishId)
                }
            }
    }
}