package ca.digixp.routes.stops

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import javax.inject.Inject

class StopsRepository @Inject constructor(private val stopsDao: StopsDao) {
  private val dbScope = CoroutineScope(Job() + Dispatchers.IO)

  fun getStopsAsync(): Deferred<List<Stop>> = dbScope.async { stopsDao.getStops() }
}