package ca.digixp.routes.stops

import ca.digixp.routes.db.COLUMN_STOP_CODE
import ca.digixp.routes.db.COLUMN_STOP_ID
import ca.digixp.routes.db.COLUMN_STOP_LATITUDE
import ca.digixp.routes.db.COLUMN_STOP_LONGITUDE
import ca.digixp.routes.db.COLUMN_STOP_NAME
import ca.digixp.routes.db.TransitDB
import ca.digixp.routes.util.getDouble
import ca.digixp.routes.util.getInt
import ca.digixp.routes.util.getString
import ca.digixp.routes.util.map
import ca.digixp.routes.util.query
import javax.inject.Inject

class StopsDaoImpl @Inject constructor(private val transitDB: TransitDB) : StopsDao {
  override fun getStops(): List<Stop> {
    return transitDB.readableDatabase.query("stops").use {
      it.map {
        Stop(
          getString(COLUMN_STOP_ID),
          getInt(COLUMN_STOP_CODE),
          getString(COLUMN_STOP_NAME),
          getDouble(COLUMN_STOP_LATITUDE),
          getDouble(COLUMN_STOP_LONGITUDE)
        )
      }
    }
  }
}