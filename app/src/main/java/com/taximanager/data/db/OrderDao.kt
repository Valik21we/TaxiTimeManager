package com.taximanager.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.taximanager.data.entity.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE shiftId = :shiftId ORDER BY timestamp DESC")
    fun getOrdersForShift(shiftId: Int): Flow<List<Order>>

    @Insert
    suspend fun insertOrder(order: Order)
}
