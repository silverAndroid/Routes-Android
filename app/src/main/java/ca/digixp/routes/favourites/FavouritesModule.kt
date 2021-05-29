package ca.digixp.routes.favourites

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FavouritesModule {
  @Binds
  abstract fun bindFavouritesDao(
    favouritesDaoImpl: FavouritesDaoImpl
  ): FavouritesDao
}