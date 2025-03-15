package org.example

import java.time.Duration
import java.time.Instant

data class AreaInfo(var timeSpentInArea: Duration, var entriesCount: Long, val timestampFirstPoint: Instant) {
    fun incrementEntriesCount() {
        this.entriesCount++
    }
}