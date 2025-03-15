package org.example

import com.charleskorn.kaml.Yaml
import com.uber.h3core.H3Core
import java.io.File
import java.time.Duration
import java.time.Instant
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

val averageEdgeLengthKm = mapOf( //resolution to edge length Km
    0 to 1281.256, 1 to 483.057, 2 to 182.513,
    3 to 68.979, 4 to 26.072, 5 to 9.854,
    6 to 3.725, 7 to 1.406, 8 to 0.531,
    9 to 0.201, 10 to 0.0759, 11 to 0.0287,
    12 to 0.0108, 13 to 0.0041, 14 to 0.0015, 15 to 0.0006
)

object Utilities {

    val H3Instance =  H3Core.newInstance()

    fun distanceFromWayPoints(point1: WayPoint, point2: WayPoint): Double {
        val R = 6371.0
        val dLat = deg2rad(point2.lat - point1.lat)  // deg2rad below
        val dLon = deg2rad(point2.lon - point1.lon)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                cos(deg2rad(point1.lat)) * cos(deg2rad(point2.lat)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c: Double = 2 * atan2(Math.sqrt(a), sqrt(1 - a));
        return R * c // Distance in km
    }

    fun deg2rad(deg: Double): Double {
        return deg * (Math.PI / 180)
    }

    fun readCsv(filePath: String): List<WayPoint> {
        val lines = File(filePath).readLines()
        return lines
            .map { line ->
                val parts = line.split(";")
                WayPoint(
                    timestamp = Instant.ofEpochMilli(parts[0].toDouble().toLong()),
                    lat = parts[1].toDouble(),
                    lon = parts[2].toDouble()
                )
            }
    }

   fun readYml(filePath: String): CustomParameters {
        val file = File(filePath);
        return Yaml.default.decodeFromString(CustomParameters.serializer(), file.readText());
    }

    fun computeMostFrequentedAreaRadiusKm(maxDistance: Double, fraction: Int): Double {
        val result = if (maxDistance < 1) 0.1 else (maxDistance / fraction)
        return String.format("%.2f", result).replace(",", ".").toDouble()
    }

    fun calculateCell(lat:Double,lon:Double,resolution:Int): Long = H3Instance.latLngToCell(lat, lon, resolution)

    // Todo: Count waypoint in the hexagon
    //TODO is better to split the functions, one for the maximum and the other for finding the map (idArea,Duration)
    fun getAreasGivenWaypoints(list: List<WayPoint>, mostFrequentedAreaRadiusKm: Double): Pair<WayPoint, Long>? {
        // if list  is empty - return null
        if (list.isEmpty()) return null

        //one element in the list
        if(list.size == 1) return Pair(list.get(0), 1);

        //calculate the best resolution given the radius
        var res: Int? = averageEdgeLengthKm.minByOrNull { (_, edgeLength) ->
            kotlin.math.abs(edgeLength - mostFrequentedAreaRadiusKm)
        }?.key
        res = res!!
        //println("Best resolution: $res")


        val mapOfAreas = mutableMapOf<Long, AreaInfo>() //AreaInfo useful to store information for each area
        //val mapOfAreas = mutableMapOf<Long, Duration>() //AreaInfo useful to store information for each area
        var pointer1 = 0
        var currentCell = calculateCell(list[pointer1].lat, list[pointer1].lon, res)
        var temp = AreaInfo(Duration.ZERO, 1, list[pointer1].timestamp) // have 1 entry in current cell
        mapOfAreas[currentCell] = temp
        //println("here: ${mapOfAreas[currentCell]}")

        for (pointer2 in 1 until list.size) {
            val nextCell = calculateCell(list[pointer2].lat, list[pointer2].lon, res)
            if (currentCell != nextCell) {  // found point in cell != current cell

                if (nextCell in mapOfAreas) {   // entry in map for next cell, increm cntr for # pts
                    val areaInfo = mapOfAreas[currentCell]!!
                    areaInfo.incrementEntriesCount()
                }
                else {  // no entry in map for next cell, create object AreaInfo for next cell
                    temp = AreaInfo(Duration.ZERO, 1, list[pointer2].timestamp)
                    mapOfAreas[nextCell] = temp
                }

                // for current cell, update timeSpentInArea with new duration
                val duration = Duration.between(list[pointer1].timestamp, list[pointer2].timestamp)
                mapOfAreas[currentCell]!!.timeSpentInArea = mapOfAreas.getOrDefault(currentCell, AreaInfo(Duration.ZERO, 1, list[pointer1].timestamp)).timeSpentInArea.plus(duration)
                pointer1 = pointer2
                currentCell = nextCell
            }
            else {   // point in same cell, increm cntr for # pts
                val areaInfo = mapOfAreas[currentCell]!!
                areaInfo.incrementEntriesCount()
            }
        }

        val duration = Duration.between(list[pointer1].timestamp, list[list.size - 1].timestamp)
        mapOfAreas[currentCell]!!.timeSpentInArea = mapOfAreas.getOrDefault(currentCell, AreaInfo(Duration.ZERO, 1, list[pointer1].timestamp)).timeSpentInArea.plus(duration)   // add to duration the time spent in the same last hexago

        // find max
        val mostFrequentedEntry = mapOfAreas.maxByOrNull { it.value.timeSpentInArea } ?: return null
        println("ID of cell (most frequented): ${mostFrequentedEntry.key}")

        val center = H3Instance.cellToLatLng(mostFrequentedEntry.key)
        println("Time spent in the area: ${mostFrequentedEntry.value.timeSpentInArea}")
        println("Center of most frequented area: $center")
        println("Number of entries: ${mostFrequentedEntry.value.entriesCount}")
        println("Timestamp of the first waypoint: ${mostFrequentedEntry.value.timestampFirstPoint}")


        val centralWaypoint =  WayPoint(mostFrequentedEntry.value.timestampFirstPoint, center.lat, center.lng)
        return Pair(centralWaypoint, mostFrequentedEntry.value.entriesCount)
    }

//    fun findMaxTimeStamp(){
//
//    }
}