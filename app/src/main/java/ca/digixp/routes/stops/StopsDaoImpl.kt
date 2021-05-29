package ca.digixp.routes.stops

import ca.digixp.routes.db.COLUMN_FAVOURITE_ID
import ca.digixp.routes.db.COLUMN_STOP_CODE
import ca.digixp.routes.db.COLUMN_STOP_ID
import ca.digixp.routes.db.COLUMN_STOP_LATITUDE
import ca.digixp.routes.db.COLUMN_STOP_LONGITUDE
import ca.digixp.routes.db.COLUMN_STOP_NAME
import ca.digixp.routes.db.DATABASE_USER
import ca.digixp.routes.db.TABLE_FAVOURITE_NAME
import ca.digixp.routes.db.TABLE_STOP_NAME
import ca.digixp.routes.db.TransitDB
import ca.digixp.routes.util.getDouble
import ca.digixp.routes.util.getInt
import ca.digixp.routes.util.getString
import ca.digixp.routes.util.getStringOrNull
import ca.digixp.routes.util.map
import javax.inject.Inject

class StopsDaoImpl @Inject constructor(private val transitDB: TransitDB) : StopsDao {
  override fun getStops(previousCode: String?, limit: Int): List<Stop> {
    var query = """SELECT s.$COLUMN_STOP_ID, s.$COLUMN_STOP_CODE, s.$COLUMN_STOP_NAME, 
      |s.$COLUMN_STOP_LATITUDE, s.$COLUMN_STOP_LONGITUDE, f.$COLUMN_FAVOURITE_ID FROM 
      |$TABLE_STOP_NAME s LEFT OUTER JOIN $DATABASE_USER.$TABLE_FAVOURITE_NAME f ON 
      |f.$COLUMN_FAVOURITE_ID = s.$COLUMN_STOP_ID WHERE s.$COLUMN_STOP_CODE > 0"""
    var selectionArgs = emptyArray<String>()
    previousCode?.let {
      query += " AND s.$COLUMN_STOP_ID >= ?"
      selectionArgs = arrayOf(previousCode)
    }
    query += " ORDER BY s.$COLUMN_STOP_ID, s.$COLUMN_STOP_CODE ASC"

    return transitDB.readableDatabase.rawQuery(query.trimMargin(), selectionArgs)
      .use {
        it.map {
          Stop(
            getString(COLUMN_STOP_ID),
            getInt(COLUMN_STOP_CODE),
            getString(COLUMN_STOP_NAME),
            getDouble(COLUMN_STOP_LATITUDE),
            getDouble(COLUMN_STOP_LONGITUDE),
            getStringOrNull(COLUMN_FAVOURITE_ID) != null
          )
        }
      }
  }
}