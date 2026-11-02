package com.taximanager.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taximanager.data.entity.Order
import com.taximanager.data.entity.Shift
import com.taximanager.viewmodel.ShiftsUiState
import com.taximanager.viewmodel.ShiftsViewModel
import com.taximanager.viewmodel.TimeFilter
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftsScreen(vm: ShiftsViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    var showEndDialog by remember { mutableStateOf(false) }
    var showAddOrderDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Shift?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Смены такси", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "${state.shifts.size} смен · ${"%.1f".format(state.totalHours)} ч",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        // Блок фильтров (Сегодня, Неделя и т.д.)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.currentFilter == TimeFilter.TODAY,
                onClick = { vm.setFilter(TimeFilter.TODAY) },
                label = { Text("Сегодня") }
            )
            FilterChip(
                selected = state.currentFilter == TimeFilter.WEEK,
                onClick = { vm.setFilter(TimeFilter.WEEK) },
                label = { Text("Неделя") }
            )
            FilterChip(
                selected = state.currentFilter == TimeFilter.MONTH,
                onClick = { vm.setFilter(TimeFilter.MONTH) },
                label = { Text("Месяц") }
            )
            FilterChip(
                selected = state.currentFilter == TimeFilter.ALL,
                onClick = { vm.setFilter(TimeFilter.ALL) },
                label = { Text("Всё время") }
            )
        }
        
        Spacer(Modifier.height(12.dp))

        SummaryCard(state)
        Spacer(Modifier.height(16.dp))

        val active = state.activeShift
        if (active != null) {
            ActiveShiftCard(
                shift = active, 
                orders = state.activeOrders,
                onEnd = { showEndDialog = true },
                onAddOrder = { showAddOrderDialog = true }
            )
            Spacer(Modifier.height(16.dp))
        } else {
            Button(
                onClick = { vm.startShift() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Начать смену", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(16.dp))
        }

        Text("История смен", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        if (state.shifts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Пока нет смен. Начни первую!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.shifts, key = { it.id }) { shift ->
                    ShiftRow(shift) { if (!shift.isActive) deleteTarget = shift }
                }
            }
        }
    }

    if (showEndDialog) {
        EndShiftDialog(
            onDismiss = { showEndDialog = false },
            onConfirm = { e, f, d, n ->
                vm.endShift(e, f, d, n); showEndDialog = false
            }
        )
    }

    if (showAddOrderDialog) {
        AddOrderDialog(
            onDismiss = { showAddOrderDialog = false },
            onConfirm = { fare, tip, isCash ->
                vm.addOrder(fare, tip, isCash)
                showAddOrderDialog = false
            }
        )
    }

    deleteTarget?.let { shift ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Удалить смену?") },
            text = { Text("Смена от ${formatDate(shift.startTime)} будет удалена.") },
            confirmButton = {
                TextButton(onClick = { vm.deleteShift(shift); deleteTarget = null }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun SummaryCard(state: ShiftsUiState) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        // SpaceEvenly раздвигает цифры
        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem("Доход", "${state.totalEarnings.toInt()} Br", Color(0xFF4CAF50))
            StatItem("Топливо", "${state.totalFuel.toInt()} Br", Color(0xFFF44336))
            StatItem("Чистыми", "${state.totalNet.toInt()} Br", MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActiveShiftCard(shift: Shift, orders: List<Order>, onEnd: () -> Unit, onAddOrder: () -> Unit) {
    var elapsed by remember { mutableStateOf(shift.durationMs) }
    LaunchedEffect(shift.id) {
        while (true) {
            elapsed = System.currentTimeMillis() - shift.startTime
            delay(1000)
        }
    }
    val h = elapsed / 3_600_000
    val m = (elapsed / 60_000) % 60
    val s = (elapsed / 1000) % 60
    
    val ordersTotal = orders.sumOf { it.totalAmount }
    val ordersTips = orders.sumOf { it.tip }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Смена идёт", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)
            Text("%02d:%02d:%02d".format(h, m, s),
                fontSize = 42.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            
            if (orders.isNotEmpty()) {
                Text("Заказов: ${orders.size} | Сумма: ${ordersTotal.toInt()} Br | Чаевые: ${ordersTips.toInt()} Br",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp))
            }

            Spacer(Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = onAddOrder,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Записать заказ", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onEnd,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Stop, null)
                Spacer(Modifier.width(8.dp))
                Text("Завершить смену", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShiftRow(shift: Shift, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(formatDate(shift.startTime), fontWeight = FontWeight.SemiBold)
                Text("${"%.1f".format(shift.durationMs / 3_600_000.0)} ч · ${shift.distanceKm.toInt()} км",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${shift.earnings.toInt()} Br", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                Text("-${shift.fuelCost.toInt()} Br", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF44336))
            }
        }
    }
}

@Composable
private fun AddOrderDialog(onDismiss: () -> Unit, onConfirm: (Double, Double, Boolean) -> Unit) {
    var fare by remember { mutableStateOf("") }
    var given by remember { mutableStateOf("") }
    var isCash by remember { mutableStateOf(false) }

    val fareDouble = fare.toDoubleOrNull() ?: 0.0
    val givenDouble = given.toDoubleOrNull() ?: 0.0
    val tip = if (isCash && givenDouble > fareDouble) givenDouble - fareDouble else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый заказ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(fare, { fare = it },
                    label = { Text("Стоимость поездки, Br") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = !isCash, onClick = { isCash = false })
                    Text("Карта", modifier = Modifier.padding(end = 16.dp))
                    RadioButton(selected = isCash, onClick = { isCash = true })
                    Text("Наличные")
                }

                if (isCash) {
                    OutlinedTextField(given, { given = it },
                        label = { Text("Сколько дали, Br") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                    
                    if (tip > 0) {
                        Text("Чаевые: ${tip.toInt()} Br", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (fareDouble > 0) {
                    onConfirm(fareDouble, tip, isCash)
                }
            }) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun EndShiftDialog(onDismiss: () -> Unit, onConfirm: (Double, Double, Double, String) -> Unit) {
    var earnings by remember { mutableStateOf("") }
    var fuel by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Завершить смену") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Введи общие данные за смену", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(earnings, { earnings = it },
                    label = { Text("Общая выручка, Br") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(fuel, { fuel = it },
                    label = { Text("Топливо, Br") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(distance, { distance = it },
                    label = { Text("Километраж, км") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(note, { note = it },
                    label = { Text("Заметка") },
                    modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    earnings.toDoubleOrNull() ?: 0.0,
                    fuel.toDoubleOrNull() ?: 0.0,
                    distance.toDoubleOrNull() ?: 0.0,
                    note
                )
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd.MM HH:mm", Locale("ru")).format(Date(timestamp))
