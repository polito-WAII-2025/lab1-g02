package org.example
import java.time.Instant

fun main() {
    //val currentDir = System.getProperty("user.dir")
    //println("Current directory: $currentDir")
    val waypoints = Utilities.readCsv("./src/main/resources/csv/waypoints_v2.csv") //./gradlew run inside RouteAnalyzer folder
    //println("Punti letti dal file:\n $waypoints")
    val maxDistance = maxDistanceFromStart(waypoints)
    println("Max distance from start: $maxDistance")
    //print( "numero di punti: ${WaypointsOutsideGeofence(WayPoint(Instant.now(),7.6935875084573695,45.091909939688094),4.5,waypoints).size}")
    print( "numero di punti: ${waypointsOutsideGeofence(WayPoint(Instant.now(),45.05330, 7.66740),5.76,waypoints).size}")
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

fun waypointsOutsideGeofence(centre:WayPoint,radius:Double,listOfWayPoints:List<WayPoint> ):List<WayPoint>{
    val outsideWayPoints = mutableListOf<WayPoint>()
    for(waypoint in listOfWayPoints){
        //print("Distanza: ${Utilities.distanceFromWayPoints(centre,waypoint)}")
        if(Utilities.distanceFromWayPoints(centre,waypoint)>radius) {
            outsideWayPoints.add(waypoint)
            //println(waypoint.toString())
        }
    }
    return outsideWayPoints
}