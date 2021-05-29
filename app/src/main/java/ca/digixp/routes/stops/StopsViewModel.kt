package ca.digixp.routes.stops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class StopsViewModel @Inject constructor(private val stopsDao: StopsDao) : ViewModel() {
  fun fetchStops(): Flow<PagingData<Stop>> {
    return Pager(
      PagingConfig(20)
    ) { StopsPagingSource(stopsDao, null) }
      .flow
      .cachedIn(viewModelScope)
  }
}