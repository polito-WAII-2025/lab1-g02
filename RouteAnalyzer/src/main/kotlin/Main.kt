package org.example

import com.uber.h3core.H3Core
import org.example.Utilities.computeMostFrequentedAreaRadiusKm
import java.time.Instant
import java.time.Duration

fun main() {
    //val currentDir = System.getProperty("user.dir")
    //println("Current directory: $currentDir")

    val customParameters = Utilities.readYml("./src/main/resources/yml/custom-parameters.yml")
    val waypoints = Utilities.readCsv("./src/main/resources/csv/waypoints.csv")

    //println("Punti letti dal file:\n $waypoints")
    //println("$customParameters");

    val maxDistance = maxDistanceFromStart(waypoints)
    println("Max distance from start: $maxDistance")

    val newInputParameters = computeMostFrequentedAreaRadiusKm(maxDistance,10)
    customParameters.mostFrequentedAreaRadiusKm ?: customParameters.setMostFrequentedAreaRadiusKm(newInputParameters)
    print("nuovi $customParameters")

    println( "numero di punti: " +
            "${waypointsOutsideGeofence(WayPoint(Instant.now(), 45.05330, 7.66740),5.76, waypoints).size}"
    )

    println(getAreasGivenWaypoints(waypoints))

}

//Calculate the farthest distance from the starting point of the route.
fun maxDistanceFromStart(waypoints: List<WayPoint>): Double {
    val startingPoint = waypoints.first()
    val remainingPoints = waypoints.drop(1) //Remove the first element
    //val R = 6371.0 // Radius of the earth in km
    var max = 0.0


    for (waypoint in remainingPoints) {

        val d = Utilities.distanceFromWayPoints(startingPoint, waypoint)

        //println("Distanza da (${startingPoint.lat}, ${startingPoint.lon}) a (${waypoint.lat}, ${waypoint.lon}) = %.3f km".format(d))

        if (d > max) {
            max = d
        }
    }
    return max
}

fun waypointsOutsideGeofence(centre: WayPoint, radius: Double, listOfWayPoints: List<WayPoint>): List<WayPoint> {
    val outsideWayPoints = mutableListOf<WayPoint>()
    for (waypoint in listOfWayPoints) {
        //print("Distanza: ${Utilities.distanceFromWayPoints(centre,waypoint)}")
        if (Utilities.distanceFromWayPoints(centre, waypoint) > radius) {
            outsideWayPoints.add(waypoint)
            //println(waypoint.toString())
        }
    }
    return outsideWayPoints
}

// Todo: Count waypoint in the hexagon
fun getAreasGivenWaypoints(list: List<WayPoint>): WayPoint? {
    if (list.size < 2) return null

    val mapOfTimeForArea = mutableMapOf<Long, Duration>()
    val h3 = H3Core.newInstance()
    var pointer1 = 0
    var currentCell = h3.latLngToCell(list[pointer1].lat, list[pointer1].lon, 9)

    for (pointer2 in 1 until list.size) {
        val nextCell = h3.latLngToCell(list[pointer2].lat, list[pointer2].lon, 9)
        if (currentCell != nextCell) {
            // println("primoPointer: \$pointer1 secondo point: \$pointer2 valori: \${list[pointer1].timestamp} , \${list[pointer2].timestamp}")
            val duration = Duration.between(list[pointer1].timestamp, list[pointer2].timestamp)
            mapOfTimeForArea[currentCell] = mapOfTimeForArea.getOrDefault(currentCell, Duration.ZERO).plus(duration)
            pointer1 = pointer2
            currentCell = nextCell
        }
    }

    val mostFrequentedEntry = mapOfTimeForArea.maxByOrNull { it.value } ?: return null
    println(mostFrequentedEntry.key)
    val center = h3.cellToLatLng(mostFrequentedEntry.key)
    println(center)

    println(mapOfTimeForArea)
    return null
}



