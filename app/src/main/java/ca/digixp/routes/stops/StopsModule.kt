package ca.digixp.routes.stops

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class StopsModule {
  @Binds
  abstract fun bindStopsDao(
    stopsDaoImpl: StopsDaoImpl
  ): StopsDao
}