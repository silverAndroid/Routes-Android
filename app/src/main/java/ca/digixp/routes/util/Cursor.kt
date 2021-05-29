package ca.digixp.routes.util

import android.database.Cursor

fun Cursor.getString(columnName: String) = getString(getColumnIndex(columnName))

fun Cursor.getStringOrNull(columnName: String) = getNullableColumn(columnName, ::getString)

fun Cursor.getFloat(columnName: String) = getFloat(getColumnIndex(columnName))

fun Cursor.getDouble(columnName: String) = getDouble(getColumnIndex(columnName))

fun Cursor.getInt(columnName: String) = getInt(getColumnIndex(columnName))

fun Cursor.getBoolean(columnIndex: Int) = getInt(columnIndex) == 1

fun Cursor.getBoolean(columnName: String) = getInt(columnName) == 1

private fun <T> Cursor.getNullableColumn(columnName: String, getValueFunction: (Int) -> T): T? {
  val index = getColumnIndex(columnName)
  return if (index > -1) getValueFunction(index) else null
}

inline fun <R> Cursor.map(transform: Cursor.() -> R): List<R> {
  if (!moveToFirst()) return emptyList()

  val list = mutableListOf<R>()
  do {
    list.add(transform())
  } while (moveToNext())

  return list
}

inline fun <T> Cursor.fold(initialValue: T, operation: (T, Cursor) -> T): T {
  if (!moveToFirst()) return initialValue

  var accumulator = initialValue
  do {
    accumulator = operation(accumulator, this)
  } while (moveToNext())

  return accumulator
}

inline fun Cursor.forEach(action: Cursor.() -> Unit) {
  if (!moveToFirst()) return

  do {
    action(this)
  } while (moveToNext())
}

inline fun <R> Cursor.useFirst(block: (Cursor) -> R): R {
  moveToFirst()
  return use(block)
}

fun Boolean.toInt() = if (this) 1 else 0