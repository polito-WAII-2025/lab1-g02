package org.example

import org.example.Utilities.computeMostFrequentedAreaRadiusKm
import java.time.Instant
import kotlinx.serialization.json.Json
import java.io.File


fun main(args: Array<String>) {

    if (args.size != 2) {
        println(
        """
        Error: Invalid number of arguments.
        Correct usage: <custom-parameters.yml> <waypoints.csv>
        Arguments received (${args.size}): ${args.joinToString()}
        """.trimIndent()
        )
        return
    }

    try {
        //read YML file
        val customParameters = Utilities.readYml(args[0])

        //read CSV file
        val waypointsList = Utilities.readCsv(args[1])

        // func 1
        val (maxDistance, mostDistantWaypoint) = maxDistanceFromStart(waypointsList, customParameters.earthRadiusKm)
        println("Max distance from start: ${maxDistance}, Point: $mostDistantWaypoint")


        customParameters.mostFrequentedAreaRadiusKm ?: customParameters.setMostFrequentedAreaRadiusKm(
            computeMostFrequentedAreaRadiusKm(maxDistance, 10)
        )

        // func 2
        val (centralWayPoint, entriesCount) = mostFrequentedArea(
            waypointsList,
            customParameters.mostFrequentedAreaRadiusKm!!
        ) ?: Pair(WayPoint(Instant.now(), 0.0, 0.0), 0L)

        // func 3
        val wayPointGeofence = WayPoint(
            Instant.ofEpochMilli(0),
            customParameters.geofenceCenterLatitude,
            customParameters.geofenceCenterLongitude
        )
        val listWaypointsOutsideGeofence = waypointsOutsideGeofence(wayPointGeofence, customParameters.geofenceRadiusKm, waypointsList, customParameters.earthRadiusKm)

        println("Number of points outside Geofence: " + "${listWaypointsOutsideGeofence.size}")

        //write results in the output.json file
        val output = OutputJson(
            maxDistanceFromStart = MaxDistanceFromStart(
                mostDistantWaypoint,
                maxDistance
            ),
            mostFrequentedArea = MostFrequentedArea(
                centralWayPoint,
                customParameters.mostFrequentedAreaRadiusKm!!,
                entriesCount
            ),
            waypointsOutsideGeofence = WaypointsOutsideGeofence(
                wayPointGeofence,
                customParameters.geofenceRadiusKm,
                listWaypointsOutsideGeofence.size,
                listWaypointsOutsideGeofence

            )
        )

        val json = Json { prettyPrint = true }
        //File("./src/main/resources/json/output.json").writeText(json.encodeToString(output)) //RUN from command line
        File("./resources/json/output.json").writeText(json.encodeToString(output)) //RUN WITH DOCKER!
    }
    catch (e: Exception) {
        println(e)
        return
    }

}

//Calculate the farthest distance from the starting point of the route.
fun maxDistanceFromStart(waypoints: List<WayPoint>, earthRadiusKm: Double):  Pair<Double, WayPoint> {

    if (waypoints.isEmpty()) {
        throw IllegalArgumentException("Error in function maxDistanceFromStart: The waypoint list is empty")
    }

    val startingPoint = waypoints.first()
    val remainingPoints = waypoints.drop(1) //Remove the first element

    var max = 0.0
    var mostDistantWaypoint: WayPoint = startingPoint


    for (waypoint in remainingPoints) {
        val d = Utilities.distanceFromWayPoints(startingPoint, waypoint, earthRadiusKm)
        if (d > max) {
            max = d
            mostDistantWaypoint = waypoint
        }
    }

    return Pair(max, mostDistantWaypoint)
}

fun mostFrequentedArea(list: List<WayPoint>, mostFrequentedAreaRadiusKm: Double): Pair<WayPoint, Long>? {

    // list empty
    if (list.isEmpty()) {
        throw IllegalArgumentException("Error in function mostFrequentedArea: The waypoint list is empty")
    }

    if(mostFrequentedAreaRadiusKm <= 0) {
        throw IllegalArgumentException("Error in function mostFrequentedArea: The mostFrequentedAreaRadiusKm must be greater than 0.")
    }
    //one element in the list
    if(list.size == 1) {
        return Pair(list.first(), 1)
    }

    val res: Int;
    try {
        //calculate the best resolution given the radius
        res = Utilities.computeResolution(mostFrequentedAreaRadiusKm)

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
    catch (e: Exception) {
        throw e
    }
}

fun waypointsOutsideGeofence(centre: WayPoint, radius: Double, listOfWayPoints: List<WayPoint>, earthRadiusKm: Double): List<WayPoint> {
    require(radius > 0) { "Error in function waypointsOutsideGeofence: Radius must be greater than zero." }
    return listOfWayPoints.filter { Utilities.distanceFromWayPoints(centre, it, earthRadiusKm) > radius }
}
