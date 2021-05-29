package ca.digixp.routes.stops

interface StopsDao {
  fun getStops(previousCode: String?, limit: Int): List<Stop>
}