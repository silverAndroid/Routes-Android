package ca.digixp.routes.stops

import androidx.paging.PagingSource
import androidx.paging.PagingState

class StopsPagingSource(private val stopsDao: StopsDao, private val query: String?) :
  PagingSource<String, Stop>() {
  override fun getRefreshKey(state: PagingState<String, Stop>): String? {
    return state.firstItemOrNull()?.id
  }

  override suspend fun load(params: LoadParams<String>): LoadResult<String, Stop> {
    try {
      val lastKey = params.key
      val response = stopsDao.getStops(lastKey, params.loadSize)
      val nextKey = try {
        response.last().id
      } catch (e: NoSuchElementException) {
        null
      }

      return LoadResult.Page(
        data = response,
        prevKey = null, // Only paging forward.
        nextKey = nextKey
      )
    } catch (e: Exception) {
      // Handle errors in this block and return LoadResult.Error if it is an
      // expected error (such as a network failure).
      throw e
    }
  }
}