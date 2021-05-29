package ca.digixp.routes.stops

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StopsViewModel @Inject constructor(private val stopsRepository: StopsRepository) :
  ViewModel() {
  private val _stops = MutableLiveData<List<Stop>>()
  val stops: LiveData<List<Stop>> = _stops

  fun fetchStops() {
    viewModelScope.launch {
      _stops.postValue(stopsRepository.getStopsAsync().await())
    }
  }
}