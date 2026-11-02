package com.taximanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taximanager.data.db.AppDatabase
import com.taximanager.data.entity.Shift
import com.taximanager.data.repository.ShiftsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShiftsUiState(
    val shifts: List<Shift> = emptyList(),
    val activeShift: Shift? = null,
    val totalEarnings: Double = 0.0,
    val totalFuel: Double = 0.0,
    val totalNet: Double = 0.0,
    val totalHours: Double = 0.0
)

class ShiftsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ShiftsRepository(AppDatabase.getDatabase(app).shiftDao())

    val uiState: StateFlow<ShiftsUiState> = combine(
        repo.allShifts,
        repo.activeShift
    ) { shifts, active ->
        val done = shifts.filter { it.endTime != null }
        ShiftsUiState(
            shifts = shifts,
            activeShift = active,
            totalEarnings = done.sumOf { it.earnings },
            totalFuel = done.sumOf { it.fuelCost },
            totalNet = done.sumOf { it.netIncome },
            totalHours = done.sumOf { it.durationMs / 3_600_000.0 }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShiftsUiState())

    fun startShift() = viewModelScope.launch { repo.startShift() }
    fun endShift(e: Double, f: Double, d: Double, n: String) =
        viewModelScope.launch { repo.endShift(e, f, d, n) }
    fun deleteShift(shift: Shift) = viewModelScope.launch { repo.deleteShift(shift) }
}
