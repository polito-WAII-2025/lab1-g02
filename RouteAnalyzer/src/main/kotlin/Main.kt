package org.example
import java.time.Instant

import java.io.File

fun main() {
   println("Hello World! ${readCsv("./RouteAnalyzer/src/main/resources/csv/waypoints.csv")}")

}

fun readCsv(filePath: String): List<WayPoint> {
    val lines = File(filePath).readLines()
    return lines
        .map { line ->
            val parts = line.split(";")
            WayPoint(
                timestamp = Instant.ofEpochMilli(parts[0].toLong()),
                lat = parts[1].toDouble(),
                lon = parts[2].toDouble()
            )
        }
}

