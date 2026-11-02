package com.taximanager.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shiftId: Int,
    val fare: Double, // Стоимость поездки
    val tip: Double = 0.0, // Чаевые
    val isCash: Boolean, // true = наличные, false = карта
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalAmount: Double get() = fare + tip
}
