package ca.digixp.routes.stops

import ca.digixp.routes.favourites.Favourite
import ca.digixp.routes.favourites.FavouriteType

data class Stop(
  override val id: String,
  val code: Int,
  val name: String,
  val latitude: Double,
  val longitude: Double
) : Favourite(id, FavouriteType.STOP)
