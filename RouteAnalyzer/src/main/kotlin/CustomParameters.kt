package org.example

import kotlinx.serialization.Serializable

@Serializable
data class CustomParameters(val earthRadiusKm: Double, val geofenceCenterLatitude: Double, val geofenceCenterLongitude: Double, val geofenceRadiusKm: Double, var mostFrequentedAreaRadiusKm: Double? = null)

fun CustomParameters.setMostFrequentedAreaRadiusKm(newVal:Double){
    this.mostFrequentedAreaRadiusKm = newVal
}