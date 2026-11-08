package ru.vitaliy.belyaev.wishapp.shared.data.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver

/** База в памяти с текущей схемой. */
expect fun createTestDatabaseDriver(): SqlDriver

/** Пустая база в памяти: старую схему тест создаёт сам. */
expect fun createEmptyTestDriver(): SqlDriver

fun SqlDriver.exec(sql: String) {
    execute(identifier = null, sql = sql, parameters = 0)
}

/** Строки запроса, все значения как текст. */
fun SqlDriver.queryRows(sql: String, columns: Int): List<List<String?>> {
    return executeQuery(
        identifier = null,
        sql = sql,
        mapper = { cursor ->
            val rows = mutableListOf<List<String?>>()
            while (cursor.next().value) {
                rows.add(List(columns) { cursor.getString(it) })
            }
            QueryResult.Value(rows)
        },
        parameters = 0,
    ).value
}
