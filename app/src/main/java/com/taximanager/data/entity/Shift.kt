package com.taximanager.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val earnings: Double = 0.0,
    val fuelCost: Double = 0.0,
    val distanceKm: Double = 0.0,
    val note: String = ""
) {
    val isActive: Boolean get() = endTime == null
    val durationMs: Long get() = (endTime ?: System.currentTimeMillis()) - startTime
    val netIncome: Double get() = earnings - fuelCost
}
