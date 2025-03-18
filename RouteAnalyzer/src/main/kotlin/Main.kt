package org.example

import com.uber.h3core.H3Core
import org.example.Utilities.computeMostFrequentedAreaRadiusKm
import java.time.Instant
import java.time.Duration
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.abs


fun main() {

    val currentDir = System.getProperty("user.dir")
    println("Current directory: $currentDir")

    //read the CSV file
    val waypointsList = Utilities.readCsv("./src/main/resources/csv/waypoints_v2.csv")

    //read the YML file
    val customParameters = Utilities.readYml("./src/main/resources/yml/custom-parameters.yml")


    // func 1
    val (maxDistance, mostDistantWaypoint) = maxDistanceFromStart(waypointsList)
    println("Max distance from start: $maxDistance and the waypoint is: $mostDistantWaypoint")

    //TODO ask what to do with exceptions inside the main
    customParameters.mostFrequentedAreaRadiusKm ?: run {
        val mostFrequentedAreaRadiusKm = computeMostFrequentedAreaRadiusKm(maxDistance, 10)
        customParameters.setMostFrequentedAreaRadiusKm(mostFrequentedAreaRadiusKm)
    }

    println("nuovi $customParameters")

    // func 2
    val (centralWayPoint, entriesCount) = mostFrequentedArea(waypointsList, customParameters.mostFrequentedAreaRadiusKm!!) ?: Pair(WayPoint(Instant.now(), 0.0, 0.0), 0L)

    // func 3
    val wayPointGeofence = WayPoint(Instant.ofEpochMilli(0), customParameters.geofenceCenterLatitude, customParameters.geofenceCenterLongitude)
    val listWaypointsOutsideGeofence = waypointsOutsideGeofence(wayPointGeofence, customParameters.geofenceRadiusKm, waypointsList)

    // TODO: earth radius
    println( "numero di punti: " + "${listWaypointsOutsideGeofence.size}")


    //write results in the output.json file
    val output = OutputJson(
        maxDistanceFromStart = MaxDistanceFromStart (
            mostDistantWaypoint,
            maxDistance
        ),
        mostFrequentedArea = MostFrequentedArea (
            centralWayPoint,
            customParameters.mostFrequentedAreaRadiusKm!!,
            entriesCount
        ),
        waypointsOutsideGeofence = WaypointsOutsideGeofence (
            wayPointGeofence,
            customParameters.geofenceRadiusKm,
            listWaypointsOutsideGeofence.size,
            listWaypointsOutsideGeofence

        )
    );

    val json = Json { prettyPrint = true }
    File("./src/main/resources/json/output.json").writeText(json.encodeToString(output))
}

//Calculate the farthest distance from the starting point of the route.
fun maxDistanceFromStart(waypoints: List<WayPoint>): Pair<Double, WayPoint> {
    val startingPoint = waypoints.first()
    val remainingPoints = waypoints.drop(1) //Remove the first element
    //val R = 6371.0 // Radius of the earth in km
    var max = 0.0
    var mostDistantWaypoint: WayPoint = startingPoint


    for (waypoint in remainingPoints) {

        val d = Utilities.distanceFromWayPoints(startingPoint, waypoint)

        //println("Distanza da (${startingPoint.lat}, ${startingPoint.longitude}) a (${waypoint.lat}, ${waypoint.longitude}) = %.3f km".format(d))

        if (d > max) {
            max = d
            mostDistantWaypoint = waypoint
        }
    }
    return Pair(max, mostDistantWaypoint)
}

//TODO is better to split the functions, one for the maximum and the other for finding the map (idArea,Duration)
fun mostFrequentedArea(list: List<WayPoint>, mostFrequentedAreaRadiusKm: Double): Pair<WayPoint, Long>? {

    // list empty
    if (list.isEmpty()) return null
    //one element in the list
    if(list.size == 1) return Pair(list[0], 1)

    //calculate the best resolution given the radius
    val res: Int = Utilities.computeResolution(mostFrequentedAreaRadiusKm)

    val mapOfAreas = Utilities.computeAreaMap(list, res) //AreaInfo useful to store information for each area

    // find max
    val mostFrequentedEntry = mapOfAreas.maxByOrNull { it.value.timeSpentInArea } ?: return null
    println("ID of cell (most frequented): ${mostFrequentedEntry.key}")

    val center = Utilities.H3Instance.cellToLatLng(mostFrequentedEntry.key)
    println("Time spent in the area: ${mostFrequentedEntry.value.timeSpentInArea}")
    println("Center of most frequented area: $center")
    println("Number of entries: ${mostFrequentedEntry.value.entriesCount}")
    println("Timestamp of the first waypoint: ${mostFrequentedEntry.value.timestampFirstPoint}")


    val centralWaypoint =  WayPoint(mostFrequentedEntry.value.timestampFirstPoint, center.lat, center.lng)
    return Pair(centralWaypoint, mostFrequentedEntry.value.entriesCount)
}


//TODO: do we throw exception for negative radius? or a print statement
fun waypointsOutsideGeofence(centre: WayPoint, radius: Double, listOfWayPoints: List<WayPoint>): List<WayPoint> = listOfWayPoints.filter { Utilities.distanceFromWayPoints(centre,it)> radius }