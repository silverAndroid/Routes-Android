package ca.digixp.routes.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import androidx.core.database.sqlite.transaction
import ca.digixp.routes.favourites.FavouriteType
import ca.digixp.routes.util.insert
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject

private const val VERSION = 1

class UserDB @Inject constructor(@ApplicationContext context: Context) :
  SQLiteOpenHelper(context, "db.sqlite", null, VERSION) {
  override fun onCreate(db: SQLiteDatabase) {
    db.transaction {
      execSQL(favouriteTypesCreationSql)
      execSQL(favouritesCreationSql)

      FavouriteType.values().forEach {
        insert(
          TABLE_FAVOURITE_TYPE_NAME,
          contentValuesOf(
            COLUMN_FAVOURITE_TYPE_ID to it.ordinal,
            COLUMN_FAVOURITE_TYPE_NAME to it.name.toLowerCase(Locale.ROOT)
          )
        )
      }
    }
  }

  override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    TODO("Not yet implemented")
  }
}