import org.example.WayPoint
import org.example.maxDistanceFromStart
import org.example.mostFrequentedArea
import org.example.waypointsOutsideGeofence
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.time.Instant

class MainKtTest {

 @Test
 fun `it should calculate the max distance correctly`(){
  val waypoints = listOf(
   WayPoint(Instant.ofEpochMilli(1741880932100), 38.11429248311086, 13.355688152939223),
   WayPoint(Instant.ofEpochMilli(1741880932200), 38.11235483569607, 13.356566584233462),
   WayPoint(Instant.ofEpochMilli(1741880932300), 38.11940278368584, 13.34126787746348),
   WayPoint(Instant.ofEpochMilli(1741880932400), 38.123976205435696, 13.350586306369422),
   WayPoint(Instant.ofEpochMilli(1741880932500), 38.077975395586265, 13.506361780634279)
  )
  assertEquals(Pair(13.78966971921408,waypoints.last()), maxDistanceFromStart(waypoints))
 }

 //TODO
/* @Test
 fun `it should calculate the max distance correctly with emptyList`(){
  val waypoints = emptyList<WayPoint>()
  assertEquals(Pair(0,null), maxDistanceFromStart(waypoints))
 }*/

  @Test
  fun `it should calculate the max distance correctly with one element`(){
   val waypoints = listOf(
    WayPoint(Instant.ofEpochMilli(1741880932100), 38.11429248311086, 13.355688152939223)
   )
   assertEquals(Pair(0.0,waypoints.first()), maxDistanceFromStart(waypoints))
  }


 @Test
 fun `it should calculate the most frequented area correctly with one area`(){
  val waypoints = listOf(
   WayPoint(Instant.ofEpochMilli(1741880932100), 38.11429248311086, 13.355688152939223),
   WayPoint(Instant.ofEpochMilli(1741880932200), 38.11235483569607, 13.356566584233462),
   WayPoint(Instant.ofEpochMilli(1741880932300), 38.11940278368584, 13.34126787746348),
   WayPoint(Instant.ofEpochMilli(1741880932400), 38.123976205435696, 13.350586306369422),
   WayPoint(Instant.ofEpochMilli(1741880932500), 38.077975395586265, 13.506361780634279),
   WayPoint(Instant.ofEpochMilli(1741880932600), 38.094138408149774, 13.389413697921668),
   WayPoint(Instant.ofEpochMilli(1741880932700), 38.0915686354094, 13.44239062643706),
   WayPoint(Instant.ofEpochMilli(1741880932800), 38.07716094904629, 13.416532829098458),
   WayPoint(Instant.ofEpochMilli(1741880932900), 38.085387219886, 13.37606146272421),
   WayPoint(Instant.ofEpochMilli(1741880933000), 38.09233991479462, 13.415244881980243)
  )

  val expected = WayPoint(Instant.ofEpochMilli(1741880932100), 39.196323196541094, 13.883693715915664)
  val actual = mostFrequentedArea(waypoints,192.5503)!!

  assertEquals(expected.timestamp, actual.first.timestamp)
  assertEquals(expected.latitude, actual.first.latitude, 0.000001) // Tolleranza per i decimali
  assertEquals(expected.longitude, actual.first.longitude, 0.000001)
  assertEquals(10, actual.second)
 }

 @Test
 fun `it should calculate the most frequented area correctly with empty list`(){
  val waypoints = emptyList<WayPoint>()

  assertEquals(null, mostFrequentedArea(waypoints,10.0))

 }

 @Test
 fun `it should calculate the most frequented area correctly with one element in the list`(){
  val waypoints = listOf(
   WayPoint(Instant.ofEpochMilli(1741880932100), 38.11429248311086, 13.355688152939223),
  )
  val exp = mostFrequentedArea(waypoints,192.5503)!!
  assertEquals(1,exp.second)
  assertEquals(waypoints.first(),exp.first)
 }

 @Test
 fun `it should calculate the most frenquented area with two areas`(){
  val waypoints = listOf(
   //points of the first area
   WayPoint(Instant.ofEpochMilli(1741880932100), 38.11429248311086, 13.355688152939223),
   WayPoint(Instant.ofEpochMilli(1741880932200), 38.11235483569607, 13.356566584233462),
   WayPoint(Instant.ofEpochMilli(1741880932300), 38.11940278368584, 13.34126787746348),
   WayPoint(Instant.ofEpochMilli(1741880932400), 38.123976205435696, 13.350586306369422),
   WayPoint(Instant.ofEpochMilli(1741880933000), 38.09233991479462, 13.415244881980243),
   //points of the second area
   WayPoint(Instant.ofEpochMilli(1741880932100), 45.14723641065018, 7.601668317663126),
   WayPoint(Instant.ofEpochMilli(1741880932200), 45.23284958868609, 7.8230585364983085),
   WayPoint(Instant.ofEpochMilli(1741880932300), 45.15389058270617, 7.848421054719893),
   WayPoint(Instant.ofEpochMilli(1741880932400), 45.22040387156477, 7.644787604965472),
   WayPoint(Instant.ofEpochMilli(1741880932500), 45.09678945865113, 7.711030483090525),
   WayPoint(Instant.ofEpochMilli(1741880932600), 45.11393734222398, 7.859227360588392),
   WayPoint(Instant.ofEpochMilli(1741880932700), 45.08541595753641, 7.9424001308222785),
   WayPoint(Instant.ofEpochMilli(1741880932800), 45.03207041467067, 7.6878955468685035)
  )

  val exp = mostFrequentedArea(waypoints, 181.3266)!!
  assertEquals(8,exp.second)
  assertEquals(WayPoint(Instant.ofEpochMilli(1741880932100),45.393601053110004 ,8.065291682168793),exp.first)
 }

 @Test
 fun `it should calculate the most frenquented area with two max areas`(){
  val waypoints = listOf(
   //points of the first area
   WayPoint(Instant.ofEpochMilli(1741880932100), 38.11429248311086, 13.355688152939223),
   WayPoint(Instant.ofEpochMilli(1741880932200), 38.11235483569607, 13.356566584233462),
   WayPoint(Instant.ofEpochMilli(1741880932300), 38.11940278368584, 13.34126787746348),
   WayPoint(Instant.ofEpochMilli(1741880932400), 38.123976205435696, 13.350586306369422),
   WayPoint(Instant.ofEpochMilli(1741880933000), 38.09233991479462, 13.415244881980243),
   //points of the second area
   WayPoint(Instant.ofEpochMilli(1741880932100), 45.14723641065018, 7.601668317663126),
   WayPoint(Instant.ofEpochMilli(1741880932200), 45.23284958868609, 7.8230585364983085),
   WayPoint(Instant.ofEpochMilli(1741880932300), 45.15389058270617, 7.848421054719893),
   WayPoint(Instant.ofEpochMilli(1741880932400), 45.22040387156477, 7.644787604965472),
   WayPoint(Instant.ofEpochMilli(1741880932500), 45.09678945865113, 7.711030483090525),
  )

  val exp = mostFrequentedArea(waypoints, 181.3266)!!
  assertEquals(5,exp.second)
  assertEquals(WayPoint(Instant.ofEpochMilli(1741880932100),45.393601053110004 ,8.065291682168793),exp.first)
 }

 @Test
 fun `it should calculate waypointsOutsideGeofence function correctly`() {

  val center = WayPoint(Instant.now(),45.06184462578261,7.645947484988113)
//  val radiusKm = 4.32  // size = 2

  val radiusKm = 1.94
  val listofWayPoints = listOf(
   WayPoint(Instant.ofEpochMilli(1741880932100), 45.063322311860134, 7.67326179791641),
   WayPoint(Instant.ofEpochMilli(1741880932200), 45.056132627701146, 7.6677061246747655),
   WayPoint(Instant.ofEpochMilli(1741880932300), 45.07439443154618, 7.613585009406165),
   WayPoint(Instant.ofEpochMilli(1741880932400), 45.05204573680365, 7.603188231116974),
   WayPoint(Instant.ofEpochMilli(1741880932500), 45.03505232431888, 7.63763691133795),
   WayPoint(Instant.ofEpochMilli(1741880932600), 45.071484996483775, 7.662377144613828),
   WayPoint(Instant.ofEpochMilli(1741880932700), 45.085579035834286, 7.7029727750400525),
   WayPoint(Instant.ofEpochMilli(1741880932800), 45.083100888541026, 7.671664997244676),
   WayPoint(Instant.ofEpochMilli(1741880932900), 45.057604236599445, 7.676967429747833),
   WayPoint(Instant.ofEpochMilli(1741880933000), 45.005587835004405, 7.624230609717898)
  )
  assertEquals(8, waypointsOutsideGeofence(center,radiusKm,listofWayPoints).size)
 }



@Test
fun `it should calculate the waypointsOutsideGeofence with one element`() {

 val center = WayPoint(Instant.now(),45.06184462578261,7.645947484988113)

 val radiusKm = 1.94
 val listofWayPoints = listOf(
  WayPoint(Instant.ofEpochMilli(1741880932100), 45.063322311860134, 7.67326179791641),
 )
 assertEquals(1, waypointsOutsideGeofence(center,radiusKm,listofWayPoints).size)
}


@Test
fun `it should calculate the waypointsOutsideGeofence with 0 element`() {

 val center = WayPoint(Instant.now(),45.06184462578261,7.645947484988113)

 val radiusKm = 1.94
 val listofWayPoints = emptyList<WayPoint>()
 assertEquals(0, waypointsOutsideGeofence(center,radiusKm,listofWayPoints).size)
}
}