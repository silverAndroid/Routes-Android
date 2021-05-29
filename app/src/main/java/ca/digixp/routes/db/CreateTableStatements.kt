package ca.digixp.routes.db

val favouriteTypesCreationSql = """CREATE TABLE $TABLE_FAVOURITE_TYPE_NAME (
    |$COLUMN_FAVOURITE_TYPE_ID INTEGER PRIMARY KEY,
    |$COLUMN_FAVOURITE_TYPE_NAME TEXT NOT NULL
    |);""".trimMargin()

val favouritesCreationSql = """CREATE TABLE $TABLE_FAVOURITE_NAME (
    |$COLUMN_FAVOURITE_ID TEXT NOT NULL,
    |$COLUMN_FAVOURITE_TYPE INTEGER NOT NULL,
    |PRIMARY KEY ($COLUMN_FAVOURITE_ID, $COLUMN_FAVOURITE_TYPE),
    |FOREIGN KEY ($COLUMN_FAVOURITE_TYPE) REFERENCES $TABLE_FAVOURITE_TYPE_NAME ($COLUMN_FAVOURITE_TYPE_ID)
    |);""".trimMargin()