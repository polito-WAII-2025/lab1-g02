package org.example

import org.example.Utilities.computeMostFrequentedAreaRadiusKm
import java.time.Instant
import kotlinx.serialization.json.Json
import java.io.File
import org.example.Utilities.validateJson

fun main(args: Array<String>) {

    val isRunningInDocker = System.getenv("RUNNING_IN_DOCKER")?.toBoolean() ?: false

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

        // func 1 - maxDistanceFromStart
        val (maxDistance, mostDistantWaypoint) = maxDistanceFromStart(waypointsList, customParameters.earthRadiusKm)
        println("Max distance from start: ${maxDistance}, Point: $mostDistantWaypoint")

        customParameters.mostFrequentedAreaRadiusKm ?: run {
            val mostFrequentedAreaRadiusKm = computeMostFrequentedAreaRadiusKm(maxDistance, 10)
            customParameters.setMostFrequentedAreaRadiusKm(mostFrequentedAreaRadiusKm)
        }

        // func 2 - mostFrequentedArea
        val (centralWayPoint, entriesCount) = mostFrequentedArea(
            waypointsList,
            customParameters.mostFrequentedAreaRadiusKm!!
        ) ?: Pair(WayPoint(Instant.now(), 0.0, 0.0), 0L)

        // func 3 - waypointsOutsideGeofence
        val wayPointGeofence = WayPoint(
            Instant.ofEpochMilli(0),
            customParameters.geofenceCenterLatitude,
            customParameters.geofenceCenterLongitude
        )
        val listWaypointsOutsideGeofence = waypointsOutsideGeofence(wayPointGeofence, customParameters.geofenceRadiusKm, waypointsList, customParameters.earthRadiusKm)

        println("Number of waypoints outside Geofence: " + "${listWaypointsOutsideGeofence.size}")

        //write results of func 1, func 2 and func 3 in the file output.json
        val output = OutputJson(
            maxDistanceFromStart = MaxDistanceFromStart(
                mostDistantWaypoint,
                maxDistance
            ),
            mostFrequentedArea = FrequentedArea(
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
        val jsonString = json.encodeToString(output)

        val schemaFilePath = if (isRunningInDocker) "./evaluation/output-schema.json" else "../evaluation/output-schema.json"

        val isValid = validateJson(jsonString, schemaFilePath)
        if (isValid) {
            val outputPath = if (isRunningInDocker) "./evaluation/output.json" else "../evaluation/output.json"
            File(outputPath).writeText(json.encodeToString(output))
        }

        //extra feature - function leastFrequentedArea(), computes the least frequented area given a list of waypoints and a radius value
        val (leastCentralWayPoint, leastEntriesCount) = leastFrequentedArea(waypointsList, customParameters.mostFrequentedAreaRadiusKm!!) ?: Pair(WayPoint(Instant.now(), 0.0, 0.0), 0L)

        val advancedOutput = OutputJsonAdvanced(
            leastFrequentedArea = FrequentedArea(
                leastCentralWayPoint,
                customParameters.mostFrequentedAreaRadiusKm!!,
                leastEntriesCount
            )
        )
        val jsonStringAdvanced = json.encodeToString(advancedOutput)
        val schemaFilePathAdvanced = if (isRunningInDocker) "./evaluation/output_advanced-schema.json" else "../evaluation/output_advanced-schema.json"

        val isValidAdvanced = validateJson(jsonStringAdvanced, schemaFilePathAdvanced)
        if (isValidAdvanced) {
            val outputPathAdvanced = if (isRunningInDocker) "./evaluation/output_advanced.json" else "../evaluation/output_advanced.json"
            File(outputPathAdvanced).writeText(jsonStringAdvanced)
        }
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

fun mostFrequentedArea(list: List<WayPoint>, mostFrequentedAreaRadiusKm: Double) = frequentedArea(list, mostFrequentedAreaRadiusKm){it.value.timeSpentInArea.toMillis()}
fun leastFrequentedArea(list: List<WayPoint>, mostFrequentedAreaRadiusKm: Double) = frequentedArea(list, mostFrequentedAreaRadiusKm){-it.value.timeSpentInArea.toMillis()}

fun frequentedArea(list: List<WayPoint>, mostFrequentedAreaRadiusKm: Double, selector:(Map.Entry<Long, AreaInfo>) -> Long): Pair<WayPoint, Long>? {

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

    val res: Int
    try {
        //calculate the best resolution given the radius
        res = Utilities.computeResolution(mostFrequentedAreaRadiusKm)

        val mapOfAreas = Utilities.computeAreaMap(list, res) //AreaInfo useful to store information for each area
    // find max
    val mostFrequentedEntry = mapOfAreas.maxByOrNull(selector) ?: return null
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
