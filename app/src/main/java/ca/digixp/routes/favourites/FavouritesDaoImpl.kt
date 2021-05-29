package ca.digixp.routes.favourites

import androidx.core.content.contentValuesOf
import ca.digixp.routes.db.COLUMN_FAVOURITE_ID
import ca.digixp.routes.db.COLUMN_FAVOURITE_TYPE
import ca.digixp.routes.db.TABLE_FAVOURITE_NAME
import ca.digixp.routes.db.UserDB
import ca.digixp.routes.util.insert
import javax.inject.Inject

class FavouritesDaoImpl @Inject constructor(private val userDB: UserDB) : FavouritesDao {
  override fun addFavourite(favourite: Favourite) {
    userDB.writableDatabase.insert(
      TABLE_FAVOURITE_NAME,
      contentValuesOf(
        COLUMN_FAVOURITE_ID to favourite.id,
        COLUMN_FAVOURITE_TYPE to favourite.type.ordinal
      )
    )
  }
}