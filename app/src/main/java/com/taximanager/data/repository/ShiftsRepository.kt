package com.taximanager.data.repository

import com.taximanager.data.db.OrderDao
import com.taximanager.data.db.ShiftDao
import com.taximanager.data.entity.Order
import com.taximanager.data.entity.Shift
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ShiftsRepository(private val dao: ShiftDao, private val orderDao: OrderDao) {
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

    fun getOrdersForActiveShift(shiftId: Int): Flow<List<Order>> = orderDao.getOrdersForShift(shiftId)
    
    // ОБНОВЛЕНО: Теперь при добавлении заказа сумма смены пересчитывается автоматически
    suspend fun addOrder(shiftId: Int, fare: Double, tip: Double, isCash: Boolean) {
        orderDao.insertOrder(Order(shiftId = shiftId, fare = fare, tip = tip, isCash = isCash))
        
        // Подтягиваем смену и обновляем её заработок
        val shift = dao.getShiftById(shiftId) ?: return
        val newEarnings = shift.earnings + fare + tip
        dao.updateShift(shift.copy(earnings = newEarnings))
    }
}
