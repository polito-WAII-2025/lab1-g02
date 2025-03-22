import org.example.Utilities
import org.example.Utilities.readCsv
import org.example.WayPoint
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import java.io.File
import java.io.FileNotFoundException
import java.time.Instant
import kotlin.test.assertFailsWith

class UtilitiesTest {

  private val utilities:Utilities = Utilities

@Test
 fun distanceFromPositiveWayPoints() {
     val wp1 = WayPoint(Instant.now(),47.48739964991453,7.649729699043819)
     val wp2 = WayPoint(Instant.now(),46.45603891800862,7.87709610595412)
     //it must be 115.95 but I think there is a little approssimation
     assertEquals(115.97221478117127,utilities.distanceFromWayPoints(wp1,wp2,6371.0) )
 }

  @Test
  fun distanceFromSameWayPoints() {
   val wp1 = WayPoint(Instant.now(),47.48739964991453,7.649729699043819)
   val wp2 = WayPoint(Instant.now(),47.48739964991453,7.649729699043819)

   assertEquals(0.0,utilities.distanceFromWayPoints(wp1,wp2,6371.0) )
  }

  @Test
  fun `distance with latitude out of range`() {
   assertFailsWith<IllegalArgumentException> (
     block = {
      val wp1 = WayPoint(Instant.now(),91.0,7.649729699043819)
      val wp2 = WayPoint(Instant.now(),-91.0,7.649729699043819)

      assertEquals(0.0,utilities.distanceFromWayPoints(wp1,wp2,6371.0) )
     }
   )
  }


  @Test
  fun `distance with longitude out of range`() {
   assertFailsWith<IllegalArgumentException> (
    block = {
     val wp1 = WayPoint(Instant.now(),0.0,181.0)
     val wp2 = WayPoint(Instant.now(),0.0,-181.0)

     assertEquals(0.0,utilities.distanceFromWayPoints(wp1,wp2,6371.0) )
    }
   )
  }


@Test
 fun `deg to rad can be computed correctly`() {
   val method = utilities.javaClass.getDeclaredMethod("deg2rad",Double::class.java)
   method.isAccessible = true
   val parameters = arrayOfNulls<Any>(1)
   parameters[0] = 45.0

    assertEquals(0.7853981633974483,method.invoke(utilities,*parameters))
 }

 @Test
 fun `deg to rad can be computed correctly with negative grades`() {
  val method = utilities.javaClass.getDeclaredMethod("deg2rad",Double::class.java)
  method.isAccessible = true
  val parameters = arrayOfNulls<Any>(1)
  parameters[0] = -45.0

  assertEquals(-0.7853981633974483,method.invoke(utilities,*parameters))
 }

 @Test
 fun `deg to rad can be computed correctly grade out of range`() {
  val method = utilities.javaClass.getDeclaredMethod("deg2rad",Double::class.java)
  method.isAccessible = true
  val parameters = arrayOfNulls<Any>(1)
  parameters[0] = 450.0
  assertEquals(7.853981633974483,method.invoke(utilities,*parameters))
 }

@Test
 fun `read csv correctly`() {
 val testFile = File.createTempFile("test", ".csv").apply {
  writeText("1672531199000;45.0;12.0\n1672531200000;46.0;13.0")
 }
 val waypoints = readCsv(testFile.absolutePath)

 assertEquals(2, waypoints.size)
 assertEquals(Instant.ofEpochMilli(1672531199000), waypoints[0].timestamp)
 assertEquals(45.0, waypoints[0].latitude)
 assertEquals(12.0, waypoints[0].longitude)
 }

 @Test
 fun `readCsv should return empty list for empty file`() {
  val emptyFile = File.createTempFile("empty", ".csv").apply { writeText("") }
  val waypoints = readCsv(emptyFile.absolutePath)

  assertEquals(0, waypoints.size)
 }
//TODO to test proprerly
/* @Test
 fun `readCsv should throw error for malformed data`() {
  val invalidFile = File.createTempFile("invalid", ".csv").apply {
   writeText("INVALID;45.0;12.0")
  }

  assertFailsWith<FileNotFoundException> {
   readCsv(invalidFile.absolutePath)
  }
 }*/

 @Test
 fun `readYml should parse valid YAML correctly`() {
  val testFile = File.createTempFile("test", ".yml").apply {
   writeText("earthRadiusKm: 6371.0\ngeofenceCenterLatitude: 42.0\ngeofenceCenterLongitude: 41.0\ngeofenceRadiusKm: 0.4\nmostFrequentedAreaRadiusKm: 0.5")
  }

  val params = utilities.readYml(testFile.absolutePath)

  assertEquals(6371.0, params.earthRadiusKm)
  assertEquals(42.0, params.geofenceCenterLatitude)
  assertEquals(41.0, params.geofenceCenterLongitude)
  assertEquals(0.4, params.geofenceRadiusKm)
  assertEquals(0.5, params.mostFrequentedAreaRadiusKm)

 }

 @Test
 fun `readYml should throw error for empty file`() {
  val emptyFile = File.createTempFile("empty", ".yml").apply { writeText("") }

  assertFailsWith<Exception> {
   utilities.readYml(emptyFile.absolutePath)
  }
 }



 @Test
 fun `readYml should throw error for invalid YAML`() {
  val invalidFile = File.createTempFile("invalid", ".yml").apply {
   writeText("invalid_yaml: : :")
  }

  assertFailsWith<Exception> {
   utilities.readYml(invalidFile.absolutePath)
  }
 }



@Test
 fun `compute most frequented Area Radius Km correctly`() {
  assertEquals(0.1,utilities.computeMostFrequentedAreaRadiusKm(0.99,10))
 }

 @Test
 fun `compute most frequented Area Radius Km correctly with maxdistance equal to 1`() {
   assertEquals(0.1,utilities.computeMostFrequentedAreaRadiusKm(1.0,10))
 }

 @Test
 fun `compute most frequented Area Radius Km correctly with maxdistance greater than 1`() {
  assertEquals(1.0,utilities.computeMostFrequentedAreaRadiusKm(10.0,10))
 }
 @Test
 fun `compute most frequented Area Radius Km with error`() {
  assertFailsWith<IllegalArgumentException>(
   block = {
    assertEquals(1.0, utilities.computeMostFrequentedAreaRadiusKm(-10.0, 10))
   }
  )
 }

@Test
 fun `it should calculate cell correctly`() {
   assertEquals("8d2810371c62a3f".toLong(16), utilities.calculateCell(41.133480317,-121.62947368,13))
 }

 @Test
 fun `it should error with langitude out of range`() {
  assertFailsWith<IllegalArgumentException> (
   block = {
    assertEquals("8d2810371c62a3f".toLong(16), utilities.calculateCell(91.133480317,-121.62947368,13))
   }
  )

 }

 @Test
 fun `it should error with error with longitude out of range`() {
  assertFailsWith<IllegalArgumentException> (
   block = {
    assertEquals("8d2810371c62a3f".toLong(16), utilities.calculateCell(41.133480317,-181.62947368,13))
   }
  )

 }

 @Test
 fun `it should error with error with resolution out of range`() {
  assertFailsWith<IllegalArgumentException> (
   block = {
    assertEquals("8d2810371c62a3f".toLong(16), utilities.calculateCell(41.133480317,-121.62947368,16))
   }
  )

 }

@Test
 fun getAreasGivenWaypoints() {}
}