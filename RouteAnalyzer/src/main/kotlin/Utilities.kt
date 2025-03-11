package org.example

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Utilities {
    fun distanceFromWayPoints(point1:WayPoint,point2:WayPoint):Double {
        val R = 6371.0
        val dLat = deg2rad(point2.lat-point1.lat)  // deg2rad below
        val dLon = deg2rad(point2.lon-point1.lon)

        val a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    cos(deg2rad(point1.lat)) * cos(deg2rad(point2.lat)) *
                    sin(dLon/2) * sin(dLon/2)
        val c: Double = 2 * atan2(Math.sqrt(a), sqrt(1 - a));
        return R * c // Distance in km
    }
}