package org.example

import org.example.Utilities.computeMostFrequentedAreaRadiusKm
import java.time.Instant
import java.time.Duration
import kotlinx.serialization.json.Json
import java.io.File


fun main(args: Array<String>) {
    val currentDir = System.getProperty("user.dir")
    println("Current directory: $currentDir")

    if (args.isEmpty()) {
        println("Errore: specificare il percorso del file custom-parameters.yml")
        return
    }

    println("YML: ${args[0]} " )
    //val customParameters = Utilities.readYml("./src/main/resources/yml/custom-parameters.yml")
    val customParameters = Utilities.readYml(args[0])
    //val waypoints = Utilities.readCsv("./src/main/resources/csv/waypoints_v2.csv")
    val waypoints = Utilities.readCsv(args[1])

    //println("Punti letti dal file:\n $waypoints")
    //println("$customParameters");

    // func 1
    val (maxDistance, mostDistantWaypoint) = maxDistanceFromStart(waypoints)
    println("Max distance from start: $maxDistance")

    // read params from yml file
    val newInputParameters = computeMostFrequentedAreaRadiusKm(maxDistance,10)
    customParameters.mostFrequentedAreaRadiusKm ?: customParameters.setMostFrequentedAreaRadiusKm(newInputParameters)
    println("nuovi $customParameters")

    // func 3
    val wayPointGeofence = WayPoint(Instant.ofEpochMilli(0), customParameters.geofenceCenterLatitude, customParameters.geofenceCenterLongitude)
    val listWaypointsOutsideGeofence = waypointsOutsideGeofence(wayPointGeofence, customParameters.geofenceRadiusKm, waypoints)
    // TODO: earth radius
    println( "numero di punti: " + "${listWaypointsOutsideGeofence.size}")

    // func 2
    val (centralWayPoint, entriesCount) = mostFrequentedArea(waypoints, customParameters.mostFrequentedAreaRadiusKm!!) ?: Pair(WayPoint(Instant.now(), 0.0, 0.0), 0L)

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
    )

    val json = Json { prettyPrint = true }
    //File("./src/main/resources/json/output.json").writeText(json.encodeToString(output))
    File("./resources/json/output.json").writeText(json.encodeToString(output))

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
    // if list  is empty - return null
    if (list.isEmpty()) return null

    //one element in the list
    if(list.size == 1) return Pair(list.get(0), 1)

    //calculate the best resolution given the radius
    var res: Int? = averageEdgeLengthKm.minByOrNull { (_, edgeLength) ->
        kotlin.math.abs(edgeLength - mostFrequentedAreaRadiusKm)
    }?.key
    res = res!!
    //println("Best resolution: $res")


    val mapOfAreas = mutableMapOf<Long, AreaInfo>() //AreaInfo useful to store information for each area
    //val mapOfAreas = mutableMapOf<Long, Duration>() //AreaInfo useful to store information for each area
    var pointer1 = 0
    var currentCell = Utilities.calculateCell(list[pointer1].latitude, list[pointer1].longitude, res)
    var temp = AreaInfo(Duration.ZERO, 1, list[pointer1].timestamp) // have 1 entry in current cell
    mapOfAreas[currentCell] = temp
    //println("here: ${mapOfAreas[currentCell]}")

    for (pointer2 in 1 until list.size) {
        val nextCell = Utilities.calculateCell(list[pointer2].latitude, list[pointer2].longitude, res)
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

    val center = Utilities.H3Instance.cellToLatLng(mostFrequentedEntry.key)
    println("Time spent in the area: ${mostFrequentedEntry.value.timeSpentInArea}")
    println("Center of most frequented area: $center")
    println("Number of entries: ${mostFrequentedEntry.value.entriesCount}")
    println("Timestamp of the first waypoint: ${mostFrequentedEntry.value.timestampFirstPoint}")


    val centralWaypoint =  WayPoint(mostFrequentedEntry.value.timestampFirstPoint, center.lat, center.lng)
    return Pair(centralWaypoint, mostFrequentedEntry.value.entriesCount)
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


