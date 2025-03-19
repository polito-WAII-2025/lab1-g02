package org.example

import com.charleskorn.kaml.Yaml
import com.uber.h3core.H3Core
import java.io.File
import java.time.Duration
import java.time.Instant
import kotlin.math.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchemaFactory


val averageEdgeLengthKm = mapOf( //resolution to edge length Km
    0 to 1281.256, 1 to 483.057, 2 to 182.513,
    3 to 68.979, 4 to 26.072, 5 to 9.854,
    6 to 3.725, 7 to 1.406, 8 to 0.531,
    9 to 0.201, 10 to 0.0759, 11 to 0.0287,
    12 to 0.0108, 13 to 0.0041, 14 to 0.0015, 15 to 0.0006
)

object Utilities {

    val H3Instance =  H3Core.newInstance()

    fun distanceFromWayPoints(point1: WayPoint, point2: WayPoint): Double {
        val R = 6371.0
        val dLat = deg2rad(point2.latitude - point1.latitude)  // deg2rad below
        val dLon = deg2rad(point2.longitude - point1.longitude)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                cos(deg2rad(point1.latitude)) * cos(deg2rad(point2.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c: Double = 2 * atan2(Math.sqrt(a), sqrt(1 - a));
        return R * c // Distance in km
    }

    fun deg2rad(deg: Double): Double {
        return deg * (Math.PI / 180)
    }

    fun readCsv(filePath: String): List<WayPoint> {
        val lines = File(filePath).readLines()
        return lines
            .map { line ->
                val parts = line.split(";")
                WayPoint(
                    timestamp = Instant.ofEpochMilli(parts[0].toDouble().toLong()),
                    latitude = parts[1].toDouble(),
                    longitude = parts[2].toDouble()
                )
            }
    }

   fun readYml(filePath: String): CustomParameters {
        val file = File(filePath);
        return Yaml.default.decodeFromString(CustomParameters.serializer(), file.readText());
    }

    fun computeMostFrequentedAreaRadiusKm(maxDistance: Double, fraction: Int): Double {
        require(maxDistance>0)
        val result:Double = if (maxDistance < 1) 0.1 else (maxDistance / fraction)
        return String.format("%.2f", result).replace(",", ".").toDouble()
    }

    fun calculateCell(lat:Double,lon:Double,resolution:Int): Long {
        require(lat in -90.0..90.0) { "Latitude must be between -90 and 90.0" }
        require(lon in -180.0..180.0) { "Longitude must be between -180 and 180." }
        require(resolution in 0..15){"Resolution must be between 0 and 15 "}
        return H3Instance.latLngToCell(lat, lon, resolution)
    }

    fun computeResolution(mostFrequentedAreaRadiusKm: Double): Int {
        val res: Int = averageEdgeLengthKm.minByOrNull { (_, edgeLength) ->
            abs(edgeLength - mostFrequentedAreaRadiusKm)
        }?.key!!
        return res
    }

    fun computeAreaMap(list: List<WayPoint>, res: Int): MutableMap<Long, AreaInfo> {

        val mapOfAreas = mutableMapOf<Long, AreaInfo>() //AreaInfo useful to store information for each area

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
                } else {  // no entry in map for next cell, create object AreaInfo for next cell
                    temp = AreaInfo(Duration.ZERO, 1, list[pointer2].timestamp)
                    mapOfAreas[nextCell] = temp
                }

                // for current cell, update timeSpentInArea with new duration
                val duration = Duration.between(list[pointer1].timestamp, list[pointer2].timestamp)
                mapOfAreas[currentCell]!!.timeSpentInArea =
                    mapOfAreas.getOrDefault(currentCell, AreaInfo(Duration.ZERO, 1, list[pointer1].timestamp))
                        .timeSpentInArea.plus(duration)

                pointer1 = pointer2
                currentCell = nextCell

            } else {   // point in same cell, increm cntr for # pts
                val areaInfo = mapOfAreas[currentCell]!!
                areaInfo.incrementEntriesCount()
            }
        }

        val duration = Duration.between(list[pointer1].timestamp, list[list.size - 1].timestamp)
        mapOfAreas[currentCell]!!.timeSpentInArea =
            mapOfAreas.getOrDefault(currentCell, AreaInfo(Duration.ZERO, 1, list[pointer1].timestamp)).timeSpentInArea.plus(
                duration
            )   // add to duration the time spent in the same last hexago
        return mapOfAreas
    }

    fun validateJson(jsonString: String, schemaFilePath: String): Boolean {
        val mapper = ObjectMapper()

        // Carica lo schema JSON da file
        val schemaNode: JsonNode = mapper.readTree(File(schemaFilePath))

        // Converte la stringa JSON in un nodo JSON
        val jsonNode: JsonNode = mapper.readTree(jsonString)

        // Crea il validatore
        val schemaFactory = JsonSchemaFactory.byDefault()
        val schema = schemaFactory.getJsonSchema(schemaNode)

        // Esegui la validazione
        val report: ProcessingReport = schema.validate(jsonNode)

        // Stampa eventuali errori
        if (!report.isSuccess) {
            println("JSON non valido! Errori trovati:")
            report.forEach { println(it) }
        } else {
            println("Output JSON validato")
        }

        return report.isSuccess
    }}