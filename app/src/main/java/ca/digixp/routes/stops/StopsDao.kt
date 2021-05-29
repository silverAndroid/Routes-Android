package ca.digixp.routes.stops

interface StopsDao {
  fun getStops(): List<Stop>
}