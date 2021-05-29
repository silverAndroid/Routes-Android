package ca.digixp.routes.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import ca.digixp.routes.stops.StopsViewModel
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StopsList(stopsViewModel: StopsViewModel) {
  val stops = stopsViewModel.fetchStops().collectAsLazyPagingItems()

  LazyColumn {
    items(stops) { stop ->
      if (stop == null) {
        CircularProgressIndicator()
      } else {
        ListItem(trailing = {
          IconButton(onClick = {
            stopsViewModel.addAsFavourite(stop)
          }) {
            if (stop.isFavourite) {
              Icon(Icons.Outlined.Star, "Add to favourites", tint = Color.Red)
            } else {
              Icon(Icons.Filled.Star, "Remove from favourites", tint = Color.Yellow)
            }
          }
        }) {
          Text(text = stop.name)
        }
      }
    }
  }
}