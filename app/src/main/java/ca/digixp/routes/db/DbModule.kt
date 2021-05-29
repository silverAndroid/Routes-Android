package ca.digixp.routes.db

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DbModule {
  @Provides
  fun provideTransitDb(@ApplicationContext context: Context, userDB: UserDB): TransitDB {
    // TODO: use sharedpreferences to retrieve current city
    val city = "ottawa"

    return TransitDB(context, userDB, city)
  }
}