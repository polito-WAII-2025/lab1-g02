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

object Utilities {

    val H3Istance =  H3Core.newInstance()

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

    fun calculateCell(lat:Double,lon:Double,resolution:Int): Long = H3Istance.latLngToCell(lat, lon, resolution)

    // Todo: Count waypoint in the hexagon
    //TODO is better to split the functions, one for the maximum and the other for finding the map (idArea,Duration)
    fun getAreasGivenWaypoints(list: List<WayPoint>): WayPoint? {
        if (list.size < 2) return null

        val mapOfTimeForArea = mutableMapOf<Long, Duration>()

        var pointer1 = 0
        var currentCell = calculateCell(list[pointer1].lat, list[pointer1].lon, 1)

        for (pointer2 in 1 until list.size-1) {
            val nextCell = calculateCell(list[pointer2].lat, list[pointer2].lon, 1)
            if (currentCell != nextCell) {
                // println("primoPointer: \$pointer1 secondo point: \$pointer2 valori: \${list[pointer1].timestamp} , \${list[pointer2].timestamp}")
                val duration = Duration.between(list[pointer1].timestamp, list[pointer2].timestamp)
                mapOfTimeForArea[currentCell] = mapOfTimeForArea.getOrDefault(currentCell, Duration.ZERO).plus(duration)
                pointer1 = pointer2
                currentCell = nextCell
            }
        }
        val duration = Duration.between(list[pointer1].timestamp, list[list.size-1].timestamp)
        mapOfTimeForArea[currentCell] = mapOfTimeForArea.getOrDefault(currentCell, Duration.ZERO).plus(duration)
        println(mapOfTimeForArea)
        println(mapOfTimeForArea)
        return null
    }
    fun findMaxTimeStamp(){

    }
}