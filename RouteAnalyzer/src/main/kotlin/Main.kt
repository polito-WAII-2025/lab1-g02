package org.example

import org.example.Utilities.computeMostFrequentedAreaRadiusKm
import java.time.Instant
import kotlinx.serialization.json.Json
import java.io.File
import org.example.Utilities.validateJson

fun main(args: Array<String>) {
    val currentDir = System.getProperty("user.dir")
    println("Current directory: $currentDir")

    if (args.isEmpty()) {
        println("Errore: specificare il percorso del file custom-parameters.yml")
        return
    }

    println("YML: ${args[0]} " )
    
    val customParameters = Utilities.readYml(args[0])
    val waypointsList = Utilities.readCsv(args[1])


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
        mostFrequentedArea = FrequentedArea (
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
    val jsonString = json.encodeToString(output) // Convertiamo l'oggetto in stringa JSON

    val schemaFilePath = "./evaluation/output-schema.json"

    val isValid = validateJson(jsonString, schemaFilePath)

    if (isValid) {
        File("./evaluation/output.json").writeText(jsonString) // Salviamo il JSON solo se è valido
    } else {
        println("Il file JSON NON è conforme allo schema!")
    }

    //extra feature
    val (lessCentralWayPoint, lessEntriesCount) = lessFrequentedArea(waypointsList, customParameters.mostFrequentedAreaRadiusKm!!) ?: Pair(WayPoint(Instant.now(), 0.0, 0.0), 0L)

    val advancedOutput = OutputJsonAdvanced(
        lessFrequentedArea = FrequentedArea(
            lessCentralWayPoint,
            customParameters.mostFrequentedAreaRadiusKm!!,
            lessEntriesCount
        )
    )
    val jsonStringAdvanced = json.encodeToString(advancedOutput)
    File("./evaluation/output_advanced.json").writeText(jsonStringAdvanced)
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

fun lessFrequentedArea(list: List<WayPoint>, lessFrequentedAreaRadiusKm: Double): Pair<WayPoint, Long>? {

    // list empty
    if (list.isEmpty()) return null
    //one element in the list
    if(list.size == 1) return Pair(list[0], 1)

    //calculate the best resolution given the radius
    val res: Int = Utilities.computeResolution(lessFrequentedAreaRadiusKm)

    val mapOfAreas = Utilities.computeAreaMap(list, res) //AreaInfo useful to store information for each area

    // find min
    val lessFrequentedEntry = mapOfAreas.minByOrNull { it.value.timeSpentInArea } ?: return null
    println("ID of cell (most frequented): ${lessFrequentedEntry.key}")

    val center = Utilities.H3Instance.cellToLatLng(lessFrequentedEntry.key)
    println("Time spent in the area: ${lessFrequentedEntry.value.timeSpentInArea}")
    println("Center of most frequented area: $center")
    println("Number of entries: ${lessFrequentedEntry.value.entriesCount}")
    println("Timestamp of the first waypoint: ${lessFrequentedEntry.value.timestampFirstPoint}")


    val centralWaypoint =  WayPoint(lessFrequentedEntry.value.timestampFirstPoint, center.lat, center.lng)
    return Pair(centralWaypoint, lessFrequentedEntry.value.entriesCount)
}