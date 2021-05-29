package ca.digixp.routes.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import ca.digixp.routes.stops.StopsViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StopsList(stopsViewModel: StopsViewModel) {
  val stops = stopsViewModel.fetchStops().collectAsLazyPagingItems()

  LazyColumn {
    items(stops) { stop ->
      if (stop == null) {
        CircularProgressIndicator()
      } else {
        ListItem {
          Text(text = stop.name)
        }
      }
    }
  }
}