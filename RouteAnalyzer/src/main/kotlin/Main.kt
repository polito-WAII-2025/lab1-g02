package org.example
import jdk.jshell.execution.Util
import java.time.Instant

import java.io.File
import kotlin.math.*

fun main() {
    val currentDir = System.getProperty("user.dir")
    println("Current directory: $currentDir")
    val waypoints = readCsv("./RouteAnalyzer/src/main/resources/csv/waypoints.csv"); //./gradlew run inside RouteAnalyzer folder
    //println("Punti letti dal file:\n $waypoints");

    val maxDistance = maxDistanceFromStart(waypoints);
    println("Max distance from start: $maxDistance");
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

//Calculate the farthest distance from the starting point of the route.
fun maxDistanceFromStart(waypoints: List<WayPoint>): Double {
    val startingPoint = waypoints.first();
    val remainingPoints = waypoints.drop(1); //Remove the first element
    val R = 6371.0; // Radius of the earth in km
    var max = 0.0;


    for (waypoint in remainingPoints) {

        val d = Utilities.distanceFromWayPoints(startingPoint, waypoint)

        //println("Distanza da (${startingPoint.lat}, ${startingPoint.lon}) a (${lat2}, ${lon2}) = %.3f km".format(d))

        if (d > max) {
            max = d;
        }
    }
    return max;
}

fun deg2rad(deg: Double): Double {
    return deg * (Math.PI/180)
}
