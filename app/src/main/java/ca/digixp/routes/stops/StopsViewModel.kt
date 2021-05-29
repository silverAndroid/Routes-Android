package ca.digixp.routes.stops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import ca.digixp.routes.favourites.FavouritesDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StopsViewModel @Inject constructor(
  private val stopsDao: StopsDao,
  private val favouritesDao: FavouritesDao
) : ViewModel() {
  lateinit var stopsPagingSource: StopsPagingSource

  fun fetchStops(): Flow<PagingData<Stop>> {
    return Pager(
      PagingConfig(20)
    ) {
      stopsPagingSource = StopsPagingSource(stopsDao, null)
      stopsPagingSource
    }
      .flow
      .cachedIn(viewModelScope)
  }

  fun addAsFavourite(stop: Stop) {
    viewModelScope.launch {
      favouritesDao.addFavourite(stop)
      stopsPagingSource.invalidate()
    }
  }
}