package com.taximanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taximanager.data.db.AppDatabase
import com.taximanager.data.entity.Order
import com.taximanager.data.entity.Shift
import com.taximanager.data.repository.ShiftsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TimeFilter {
    TODAY, WEEK, MONTH, ALL
}

data class ShiftsUiState(
    val shifts: List<Shift> = emptyList(),
    val activeShift: Shift? = null,
    val activeOrders: List<Order> = emptyList(),
    val totalEarnings: Double = 0.0,
    val totalFuel: Double = 0.0,
    val totalNet: Double = 0.0,
    val totalHours: Double = 0.0,
    val currentFilter: TimeFilter = TimeFilter.TODAY
)

class ShiftsViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getDatabase(app)
    private val repo = ShiftsRepository(db.shiftDao(), db.orderDao())

    private val _activeOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _timeFilter = MutableStateFlow(TimeFilter.TODAY)

    val uiState: StateFlow<ShiftsUiState> = combine(
        repo.allShifts,
        repo.activeShift,
        _activeOrders,
        _timeFilter
    ) { shifts, active, orders, filter ->
        val doneShifts = shifts.filter { it.endTime != null }
        val filteredShifts = doneShifts.filter { isInFilter(it.startTime, filter) }

        ShiftsUiState(
            shifts = doneShifts,
            activeShift = active,
            activeOrders = orders,
            totalEarnings = filteredShifts.sumOf { it.earnings },
            totalFuel = filteredShifts.sumOf { it.fuelCost },
            totalNet = filteredShifts.sumOf { it.netIncome },
            totalHours = filteredShifts.sumOf { it.durationMs / 3_600_000.0 },
            currentFilter = filter
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShiftsUiState())

    init {
        viewModelScope.launch {
            repo.activeShift.collectLatest { shift ->
                if (shift != null) {
                    repo.getOrdersForActiveShift(shift.id).collect { orders ->
                        _activeOrders.value = orders
                    }
                } else {
                    _activeOrders.value = emptyList()
                }
            }
        }
    }

    fun setFilter(filter: TimeFilter) {
        _timeFilter.value = filter
    }

    fun startShift() = viewModelScope.launch { repo.startShift() }
    fun endShift(e: Double, f: Double, d: Double, n: String) =
        viewModelScope.launch { repo.endShift(e, f, d, n) }
    fun deleteShift(shift: Shift) = viewModelScope.launch { repo.deleteShift(shift) }
    
    // ОБНОВЛЕНО: Теперь принимает shiftId, чтобы можно было добавлять заказы в завершенные смены
    fun addOrder(shiftId: Int, fare: Double, tip: Double, isCash: Boolean) = viewModelScope.launch {
        repo.addOrder(shiftId, fare, tip, isCash)
    }

    private fun isInFilter(timestamp: Long, filter: TimeFilter): Boolean {
        if (filter == TimeFilter.ALL) return true
        
        val now = Calendar.getInstance()
        val shiftTime = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when (filter) {
            TimeFilter.TODAY -> {
                now.get(Calendar.YEAR) == shiftTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == shiftTime.get(Calendar.DAY_OF_YEAR)
            }
            TimeFilter.WEEK -> {
                val startOfWeek = now.clone() as Calendar
                startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)
                startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
                startOfWeek.set(Calendar.MINUTE, 0)
                startOfWeek.set(Calendar.SECOND, 0)
                timestamp >= startOfWeek.timeInMillis
            }
            TimeFilter.MONTH -> {
                now.get(Calendar.YEAR) == shiftTime.get(Calendar.YEAR) &&
                now.get(Calendar.MONTH) == shiftTime.get(Calendar.MONTH)
            }
            else -> true
        }
    }
}
