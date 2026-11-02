package com.taximanager.data.repository

import com.taximanager.data.db.ShiftDao
import com.taximanager.data.entity.Shift
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ShiftsRepository(private val dao: ShiftDao) {
    val allShifts: Flow<List<Shift>> = dao.getAllShifts()
    val activeShift: Flow<Shift?> = dao.getActiveShift()

    suspend fun startShift() {
        dao.insertShift(Shift(startTime = System.currentTimeMillis()))
    }

    suspend fun endShift(earnings: Double, fuelCost: Double, distanceKm: Double, note: String) {
        val active = dao.getActiveShift().first() ?: return
        dao.updateShift(
            active.copy(
                endTime = System.currentTimeMillis(),
                earnings = earnings,
                fuelCost = fuelCost,
                distanceKm = distanceKm,
                note = note
            )
        )
    }

    suspend fun deleteShift(shift: Shift) = dao.deleteShift(shift)
}
