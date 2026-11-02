package com.taximanager.data.db

import androidx.room.*
import com.taximanager.data.entity.Shift
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts ORDER BY startTime DESC")
    fun getAllShifts(): Flow<List<Shift>>

    @Query("SELECT * FROM shifts WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun getActiveShift(): Flow<Shift?>

    // Добавили метод для получения смены по ID (нужно для добавления заказов в историю)
    @Query("SELECT * FROM shifts WHERE id = :id")
    suspend fun getShiftById(id: Int): Shift?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: Shift): Long

    @Update
    suspend fun updateShift(shift: Shift)

    @Delete
    suspend fun deleteShift(shift: Shift)
}
