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
    println("nuovi $customParameters")

    println( "numero di punti: " +
            "${waypointsOutsideGeofence(WayPoint(Instant.now(), 45.05330, 7.66740),5.76, waypoints).size}"
    )

    Utilities.getAreasGivenWaypoints(waypoints, customParameters.mostFrequentedAreaRadiusKm!!)


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


