package com.example.balaji_earthmovers.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balaji_earthmovers.data.local.entity.WorkWithDetails
import com.example.balaji_earthmovers.data.local.export.ReportExporter
import com.example.balaji_earthmovers.data.local.export.ReportKpiSummary
import com.example.balaji_earthmovers.data.local.export.ReportRowData
import com.example.balaji_earthmovers.ui.theme.ExpenseRed
import com.example.balaji_earthmovers.ui.theme.InfoBlue
import com.example.balaji_earthmovers.ui.theme.ProfitGreen
import com.example.balaji_earthmovers.ui.viewmodel.BalajiViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportPeriod(val label: String) {
    ALL("All Time"),
    THIS_MONTH("This Month"),
    THIS_WEEK("This Week"),
    TODAY("Today")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: BalajiViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val allWorks by viewModel.allWorks.collectAsState()
    val allVehicles by viewModel.allVehicles.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()
    val allDrivers by viewModel.allDrivers.collectAsState()
    val totalRevenue by viewModel.totalCollectedIncome.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val netProfit by viewModel.netProfit.collectAsState()
    val totalBilled by viewModel.totalWorkBilled.collectAsState()

    var selectedPeriod by remember { mutableStateOf(ReportPeriod.ALL) }
    var selectedVehicleId by remember { mutableStateOf<String?>(null) }
    var selectedCustomerId by remember { mutableStateOf<String?>(null) }
    var selectedDriverId by remember { mutableStateOf<String?>(null) }

    var vehicleMenuExpanded by remember { mutableStateOf(false) }
    var customerMenuExpanded by remember { mutableStateOf(false) }
    var driverMenuExpanded by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    // Filter works based on selected period and entity filters
    val filteredWorks = remember(allWorks, selectedPeriod, selectedVehicleId, selectedCustomerId, selectedDriverId) {
        val cal = Calendar.getInstance()
        val periodList = when (selectedPeriod) {
            ReportPeriod.ALL -> allWorks
            ReportPeriod.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val startOfDay = cal.timeInMillis
                allWorks.filter { it.work.date >= startOfDay }
            }
            ReportPeriod.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val startOfWeek = cal.timeInMillis
                allWorks.filter { it.work.date >= startOfWeek }
            }
            ReportPeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val startOfMonth = cal.timeInMillis
                allWorks.filter { it.work.date >= startOfMonth }
            }
        }
        periodList.filter {
            (selectedVehicleId == null || it.work.vehicleId == selectedVehicleId) &&
            (selectedCustomerId == null || it.work.customerId == selectedCustomerId) &&
            (selectedDriverId == null || it.work.driverId == selectedDriverId)
        }
    }

    val periodBilled = remember(filteredWorks) {
        filteredWorks.sumOf { it.work.totalWorkAmount }
    }
    val periodHours = remember(filteredWorks) {
        filteredWorks.sumOf { it.work.totalTimeHours }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Period Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportPeriod.values().forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period.label, fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Entity Filters: Vehicle, Customer, Driver
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vehicle filter chip
                Box {
                    val selVeh = allVehicles.find { it.id == selectedVehicleId }
                    FilterChip(
                        selected = selectedVehicleId != null,
                        onClick = { vehicleMenuExpanded = true },
                        label = { Text(selVeh?.let { "${it.vehicleName} (${it.vehicleNumber})" } ?: "All Vehicles", fontSize = 11.sp) }
                    )
                    DropdownMenu(
                        expanded = vehicleMenuExpanded,
                        onDismissRequest = { vehicleMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Vehicles") },
                            onClick = {
                                selectedVehicleId = null
                                vehicleMenuExpanded = false
                            }
                        )
                        allVehicles.forEach { v ->
                            DropdownMenuItem(
                                text = { Text("${v.vehicleName} (${v.vehicleNumber})") },
                                onClick = {
                                    selectedVehicleId = v.id
                                    vehicleMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Customer filter chip
                Box {
                    val selCust = allCustomers.find { it.id == selectedCustomerId }
                    FilterChip(
                        selected = selectedCustomerId != null,
                        onClick = { customerMenuExpanded = true },
                        label = { Text(selCust?.name ?: "All Customers", fontSize = 11.sp) }
                    )
                    DropdownMenu(
                        expanded = customerMenuExpanded,
                        onDismissRequest = { customerMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Customers") },
                            onClick = {
                                selectedCustomerId = null
                                customerMenuExpanded = false
                            }
                        )
                        allCustomers.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    selectedCustomerId = c.id
                                    customerMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Driver filter chip
                Box {
                    val selDriv = allDrivers.find { it.id == selectedDriverId }
                    FilterChip(
                        selected = selectedDriverId != null,
                        onClick = { driverMenuExpanded = true },
                        label = { Text(selDriv?.driverName ?: "All Drivers", fontSize = 11.sp) }
                    )
                    DropdownMenu(
                        expanded = driverMenuExpanded,
                        onDismissRequest = { driverMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Drivers") },
                            onClick = {
                                selectedDriverId = null
                                driverMenuExpanded = false
                            }
                        )
                        allDrivers.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d.driverName) },
                                onClick = {
                                    selectedDriverId = d.id
                                    driverMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // High-Level KPI Summary Cards (2x2 Grid)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(1.5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Work Orders Billed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${"%.2f".format(if (selectedPeriod == ReportPeriod.ALL) totalBilled else periodBilled)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Income Received", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${"%.2f".format(totalRevenue)}", fontWeight = FontWeight.Bold, color = ProfitGreen, style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Fleet Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${"%.2f".format(totalExpenses)}", fontWeight = FontWeight.Bold, color = ExpenseRed, style = MaterialTheme.typography.titleMedium)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Net Operating Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "₹${"%.2f".format(netProfit)}",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (netProfit >= 0) ProfitGreen else ExpenseRed,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            // Export Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            isExporting = true
                            withContext(Dispatchers.IO) {
                                val rows = filteredWorks.mapIndexed { index, w ->
                                    ReportRowData(
                                        code = "W-${w.work.id.take(6).uppercase()}",
                                        dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(w.work.date)),
                                        category = "Work Order",
                                        title = w.work.workName.ifBlank { w.work.location },
                                        party = w.customer.name,
                                        vehicle = "${w.vehicle.vehicleName} (${w.vehicle.vehicleNumber})",
                                        driver = w.driver.driverName,
                                        hoursOrQty = w.work.totalTimeHours,
                                        rate = w.work.hourlyRate,
                                        totalAmount = w.work.totalWorkAmount,
                                        status = "Recorded",
                                        paymentMode = "General Ledger",
                                        timestamp = w.work.date
                                    )
                                }
                                val summary = ReportKpiSummary(
                                    totalRecords = rows.size,
                                    totalBilled = periodBilled,
                                    totalExpenses = totalExpenses,
                                    netMargin = netProfit,
                                    totalHours = periodHours,
                                    pendingCount = rows.count { it.status == "Pending" }
                                )
                                val res = ReportExporter.exportToPdf(context, rows, summary, "Operations_${selectedPeriod.name}")
                                withContext(Dispatchers.Main) {
                                    isExporting = false
                                    res.onSuccess { file ->
                                        Toast.makeText(context, "PDF saved to balajiearthmovers/reports: ${file.name}", Toast.LENGTH_LONG).show()
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Export failed: ${err.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isExporting = true
                            withContext(Dispatchers.IO) {
                                val rows = filteredWorks.mapIndexed { index, w ->
                                    ReportRowData(
                                        code = "W-${w.work.id.take(6).uppercase()}",
                                        dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(w.work.date)),
                                        category = "Work Order",
                                        title = w.work.workName.ifBlank { w.work.location },
                                        party = w.customer.name,
                                        vehicle = "${w.vehicle.vehicleName} (${w.vehicle.vehicleNumber})",
                                        driver = w.driver.driverName,
                                        hoursOrQty = w.work.totalTimeHours,
                                        rate = w.work.hourlyRate,
                                        totalAmount = w.work.totalWorkAmount,
                                        status = "Recorded",
                                        paymentMode = "General Ledger",
                                        timestamp = w.work.date
                                    )
                                }
                                val summary = ReportKpiSummary(
                                    totalRecords = rows.size,
                                    totalBilled = periodBilled,
                                    totalExpenses = totalExpenses,
                                    netMargin = netProfit,
                                    totalHours = periodHours,
                                    pendingCount = rows.count { it.status == "Pending" }
                                )
                                val res = ReportExporter.exportToCsv(context, rows, summary, "Operations_${selectedPeriod.name}")
                                withContext(Dispatchers.Main) {
                                    isExporting = false
                                    res.onSuccess { file ->
                                        Toast.makeText(context, "CSV saved to balajiearthmovers/reports: ${file.name}", Toast.LENGTH_LONG).show()
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Export failed: ${err.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV", fontSize = 12.sp)
                }
            }

            // Work Log List
            Text(
                "Work Logs (${filteredWorks.size} records)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )

            if (filteredWorks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No work records found for this period.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredWorks, key = { it.work.id }) { workDetails ->
                        SimpleReportWorkCard(workDetails)
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleReportWorkCard(workDetails: WorkWithDetails) {
    val work = workDetails.work
    val dateStr = remember(work.date) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(work.date))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = workDetails.customer.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "₹${"%.2f".format(work.totalWorkAmount)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${workDetails.vehicle.vehicleName} • ${workDetails.driver.driverName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$dateStr • ${work.location.ifBlank { "Site" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 10.5.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = if (work.totalTimeHours > 0) "${"%.1f".format(work.totalTimeHours)} hrs" else "Fixed Price",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        )
                    }
                }
            }
        }
    }
}
