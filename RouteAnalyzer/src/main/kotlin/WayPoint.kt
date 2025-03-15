package org.example

import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

object InstantAsLongSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("InstantAsLong", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.toEpochMilli()) // <-- EPOCH millis
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.ofEpochMilli(decoder.decodeLong())
    }
}

@Serializable
data class WayPoint(
    @Serializable(with = InstantAsLongSerializer::class)
    val timestamp:Instant,
    val latitude:Double,
    val longitude:Double
)

@Serializable
data class OutputJson(
    val maxDistanceFromStart: MaxDistanceFromStart,
    val mostFrequentedArea: MostFrequentedArea,
    val waypointsOutsideGeofence: WaypointsOutsideGeofence
)

@Serializable
data class MaxDistanceFromStart(
    val waypoint: WayPoint,
    val distanceKm: Double
)

@Serializable
data class MostFrequentedArea(
    val centralWaypoint: WayPoint,
    val areaRadiusKm: Double,
    val entriesCount: Long
)

@Serializable
data class WaypointsOutsideGeofence(
    val centralWaypoint: WayPoint,
    val areaRadiusKm: Double,
    val count: Int,
    val waypoints: List<WayPoint>
)