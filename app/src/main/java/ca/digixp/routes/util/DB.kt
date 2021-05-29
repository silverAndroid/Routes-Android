package ca.digixp.routes.util

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

fun SQLiteDatabase.insert(table: String, contentValues: ContentValues) =
  insert(table, null, contentValues)

fun SQLiteDatabase.insertOrThrow(table: String, contentValues: ContentValues) =
  insertOrThrow(table, null, contentValues)

fun SQLiteDatabase.query(
  table: String,
  columns: Array<String>? = null,
  selection: String? = null,
  selectionArgs: Array<String>? = null,
  groupBy: String? = null,
  having: String? = null,
  orderBy: String? = null,
  limit: Int? = null,
  isDistinct: Boolean = false
): Cursor {
  return query(
    isDistinct,
    table,
    columns,
    selection,
    selectionArgs,
    groupBy,
    having,
    orderBy,
    limit?.toString()
  )
}

enum class SqliteDatabaseOpenFlag {
  READWRITE,
  READONLY
}