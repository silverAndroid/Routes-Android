package ca.digixp.routes.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import ca.digixp.routes.stops.StopsViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StopsList(stopsViewModel: StopsViewModel) {
  val stops = stopsViewModel.stops.observeAsState(emptyList())

  LazyColumn {
    items(stops.value) { stop ->
      ListItem {
        Text(text = stop.name)
      }
    }
  }
}