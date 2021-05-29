package ca.digixp.routes.favourites

interface FavouritesDao {
  fun addFavourite(favourite: Favourite)
  fun removeFavourite(favourite: Favourite)
}