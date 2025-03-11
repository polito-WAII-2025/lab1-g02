package org.example
import java.time.Instant

import java.io.File
import kotlin.math.*

fun main() {
    val waypoints = readCsv("./src/main/resources/csv/waypoints_v2.csv"); //./gradlew run inside RouteAnalyzer folder
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
    val lat1 = startingPoint.lat;
    val lon1 = startingPoint.lon;
    var lat2 = 0.0;
    var lon2 = 0.0;

    for (waypoint in remainingPoints) {
        lat2 = waypoint.lat;
        lon2 = waypoint.lon;
        var dLat: Double = deg2rad(lat2 - lat1);
        var dLon: Double = deg2rad(lon2 - lon1);
        var a: Double = sin(dLat / 2) * sin(dLat / 2) +
                cos(deg2rad(lat1)) * cos(deg2rad(lat2)) *
                sin(dLon / 2) * sin(dLon / 2);
        var c: Double = 2 * atan2(Math.sqrt(a), sqrt(1 - a));
        var d: Double = R * c; // Distance in km

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
