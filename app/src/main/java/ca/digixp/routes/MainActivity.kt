package ca.digixp.routes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.digixp.routes.stops.StopsDao
import ca.digixp.routes.stops.StopsViewModel
import ca.digixp.routes.ui.StopsList
import ca.digixp.routes.ui.theme.RoutesTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  private val stopsViewModel by viewModels<StopsViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    stopsViewModel.fetchStops()
    setContent {
      RoutesTheme {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {
          StopsList(stopsViewModel = stopsViewModel)
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String) {
  Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  RoutesTheme {
    Greeting("Android")
  }
}