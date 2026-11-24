package com.example.balaji_earthmovers.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.balaji_earthmovers.data.local.entity.VehicleEntity
import com.example.balaji_earthmovers.data.local.entity.WorkEntity
import com.example.balaji_earthmovers.data.local.entity.WorkWithDetails
import com.example.balaji_earthmovers.ui.theme.AmberPrimary
import com.example.balaji_earthmovers.ui.theme.ExpenseRed
import com.example.balaji_earthmovers.ui.theme.InfoBlue
import com.example.balaji_earthmovers.ui.theme.ProfitGreen
import com.example.balaji_earthmovers.ui.util.CustomerShareUtils
import com.example.balaji_earthmovers.ui.util.EntityAvatar
import com.example.balaji_earthmovers.ui.util.LocalStrings
import com.example.balaji_earthmovers.ui.viewmodel.BalajiViewModel
import com.example.balaji_earthmovers.ui.viewmodel.FleetSeasonFilter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetWorkScreen(
    viewModel: BalajiViewModel,
    onOpenNewWorkDialog: () -> Unit,
    initialTab: Int = 0
) {
    val strings = LocalStrings.current
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var vehicleToEditPending by remember { mutableStateOf<VehicleEntity?>(null) }
    var vehicleToEdit by remember { mutableStateOf<VehicleEntity?>(null) }
    var vehicleToDelete by remember { mutableStateOf<VehicleEntity?>(null) }
    var vehicleForDetail by remember { mutableStateOf<VehicleEntity?>(null) }

    var workToEditPending by remember { mutableStateOf<WorkWithDetails?>(null) }
    var workToEdit by remember { mutableStateOf<WorkWithDetails?>(null) }
    var workToDelete by remember { mutableStateOf<WorkWithDetails?>(null) }
    var workForDetail by remember { mutableStateOf<WorkWithDetails?>(null) }

    val vehicles by viewModel.filteredVehicles.collectAsState()
    val works by viewModel.allWorks.collectAsState()
    val seasonFilter by viewModel.seasonFilter.collectAsState()

    val filteredWorks = remember(works, seasonFilter) {
        when (seasonFilter) {
            FleetSeasonFilter.ALL -> works
            FleetSeasonFilter.THIS_MONTH -> {
                val cal = Calendar.getInstance()
                val curMonth = cal.get(Calendar.MONTH)
                val curYear = cal.get(Calendar.YEAR)
                works.filter {
                    cal.timeInMillis = it.work.date
                    cal.get(Calendar.MONTH) == curMonth && cal.get(Calendar.YEAR) == curYear
                }
            }
            FleetSeasonFilter.MONSOON -> {
                val cal = Calendar.getInstance()
                works.filter {
                    cal.timeInMillis = it.work.date
                    val m = cal.get(Calendar.MONTH)
                    m in 5..8 // June-September
                }
            }
            FleetSeasonFilter.PEAK_CONSTRUCTION -> {
                val cal = Calendar.getInstance()
                works.filter {
                    cal.timeInMillis = it.work.date
                    val m = cal.get(Calendar.MONTH)
                    m in 9..11 || m in 0..4 // October-May
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddVehicleDialog = true else onOpenNewWorkDialog()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = if (selectedTab == 0) strings.addVehicle else strings.newWork
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("${strings.tabVehicles} (${vehicles.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("${strings.tabWork} (${filteredWorks.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedTab == 0) {
                // Seasonal Filter Row for Vehicles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FleetSeasonFilter.values().forEach { filter ->
                        FilterChip(
                            selected = seasonFilter == filter,
                            onClick = { viewModel.setSeasonFilter(filter) },
                            label = { Text(filter.displayName) }
                        )
                    }
                }

                // Vehicles List
                if (vehicles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No vehicles registered. Tap '+' to add vehicle.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(vehicles, key = { it.id }) { vehicle ->
                            VehicleCard(
                                vehicle = vehicle,
                                onClick = { vehicleForDetail = vehicle }
                            )
                        }
                    }
                }
            } else {
                // Seasonal Filter Row for Work
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FleetSeasonFilter.values().forEach { filter ->
                        FilterChip(
                            selected = seasonFilter == filter,
                            onClick = { viewModel.setSeasonFilter(filter) },
                            label = { Text(filter.displayName) }
                        )
                    }
                }

                // Work Entries List
                if (filteredWorks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No work entries logged. Tap '+' to add work.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredWorks, key = { it.work.id }) { workDetails ->
                            WorkEntryDetailCard(
                                workDetails = workDetails,
                                onClick = { workForDetail = workDetails }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddVehicleDialog) {
        VehicleFormDialog(
            vehicleToEdit = null,
            viewModel = viewModel,
            onDismiss = { showAddVehicleDialog = false }
        )
    }

    vehicleForDetail?.let { v ->
        VehicleDetailDialog(
            vehicle = v,
            onEdit = {
                vehicleToEditPending = v
                vehicleForDetail = null
            },
            onDelete = {
                vehicleToDelete = v
                vehicleForDetail = null
            },
            onMarkServiced = {
                viewModel.markVehicleServiced(v.id)
                vehicleForDetail = null
            },
            onDismiss = { vehicleForDetail = null }
        )
    }

    vehicleToEditPending?.let { v ->
        AdminPasswordEditDialog(
            itemType = "Vehicle",
            itemName = "${v.vehicleName} (${v.vehicleNumber})",
            viewModel = viewModel,
            onDismiss = { vehicleToEditPending = null },
            onVerified = {
                vehicleToEdit = v
                vehicleToEditPending = null
            }
        )
    }

    vehicleToEdit?.let { v ->
        VehicleFormDialog(
            vehicleToEdit = v,
            viewModel = viewModel,
            onDismiss = { vehicleToEdit = null }
        )
    }

    vehicleToDelete?.let { v ->
        AdminPasswordDeleteDialog(
            itemType = "Vehicle",
            itemName = "${v.vehicleName} (${v.vehicleNumber})",
            viewModel = viewModel,
            onDismiss = { vehicleToDelete = null },
            onConfirmDelete = {
                viewModel.deleteVehicle(v.id)
            }
        )
    }

    workForDetail?.let { w ->
        WorkDetailDialog(
            workDetails = w,
            onEdit = {
                workToEditPending = w
                workForDetail = null
            },
            onDelete = {
                workToDelete = w
                workForDetail = null
            },
            onDismiss = { workForDetail = null }
        )
    }

    workToEditPending?.let { w ->
        AdminPasswordEditDialog(
            itemType = "Work",
            itemName = w.work.workName,
            viewModel = viewModel,
            onDismiss = { workToEditPending = null },
            onVerified = {
                workToEdit = w
                workToEditPending = null
            }
        )
    }

    workToEdit?.let { w ->
        EditWorkDialog(
            workDetails = w,
            viewModel = viewModel,
            onDismiss = { workToEdit = null }
        )
    }

    workToDelete?.let { w ->
        AdminPasswordDeleteDialog(
            itemType = "Work",
            itemName = w.work.workName,
            viewModel = viewModel,
            onDismiss = { workToDelete = null },
            onConfirmDelete = {
                viewModel.deleteWork(w.work.id)
            }
        )
    }
}

@Composable
fun VehicleCard(
    vehicle: VehicleEntity,
    onClick: () -> Unit
) {
    val isOverdue = vehicle.isServiceDue
    val hoursSinceService = vehicle.hoursSinceLastService
    val progress = (hoursSinceService / 250.0).coerceIn(0.0, 1.0).toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) ExpenseRed.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isOverdue) androidx.compose.foundation.BorderStroke(1.5.dp, ExpenseRed) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EntityAvatar(
                        imageUri = vehicle.imageUri,
                        fallbackIcon = Icons.Default.DirectionsCar,
                        contentDescription = vehicle.vehicleName,
                        size = 50.dp
                    )
                    Column {
                        Text(vehicle.vehicleName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Number: ${vehicle.vehicleNumber} • Model: ${vehicle.model}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        "Tap for details",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isOverdue) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ExpenseRed,
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text("250h SERVICE DUE!", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Service Interval Tracker Progress
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Service Interval: ${"%.1f".format(hoursSinceService)} / 250 hrs",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOverdue) ExpenseRed else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (isOverdue) "Overdue by ${"%.1f".format(hoursSinceService - 250.0)} hrs" else "${"%.1f".format(250.0 - hoursSinceService)} hrs left",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) ExpenseRed else ProfitGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isOverdue) ExpenseRed else AmberPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Worked Hours & Fuel Total Counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = InfoBlue)
                    Text("Worked: ${vehicle.workedHours} hrs", style = MaterialTheme.typography.bodySmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(16.dp), tint = AmberPrimary)
                    Text("Fuel: ₹${"%.0f".format(vehicle.fuelExpenseTotal)}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun VehicleDetailDialog(
    vehicle: VehicleEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkServiced: () -> Unit,
    onDismiss: () -> Unit
) {
    val isOverdue = vehicle.isServiceDue
    val hoursSinceService = vehicle.hoursSinceLastService
    val progress = (hoursSinceService / 250.0).coerceIn(0.0, 1.0).toFloat()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EntityAvatar(
                    imageUri = vehicle.imageUri,
                    fallbackIcon = Icons.Default.DirectionsCar,
                    contentDescription = vehicle.vehicleName,
                    size = 52.dp
                )
                Column {
                    Text(vehicle.vehicleName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(vehicle.vehicleNumber, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Model / Type:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(vehicle.model.ifBlank { "Standard" }, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Last Service At:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${"%.1f".format(vehicle.lastServiceHours)} hrs", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = ProfitGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Worked Hours:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${vehicle.workedHours} hrs", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = InfoBlue)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Fuel Expense:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${"%.0f".format(vehicle.fuelExpenseTotal)}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = AmberPrimary)
                        }
                    }
                }

                // Service Tracker
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Service Interval: ${"%.1f".format(hoursSinceService)} / 250 hrs",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOverdue) ExpenseRed else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            if (isOverdue) "Overdue by ${"%.1f".format(hoursSinceService - 250.0)} hrs" else "${"%.1f".format(250.0 - hoursSinceService)} hrs left",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) ExpenseRed else ProfitGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isOverdue) ExpenseRed else AmberPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onMarkServiced,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset 250h Service Counter", style = MaterialTheme.typography.labelMedium)
                }

                HorizontalDivider()

                // Actions Inside Card: Edit and Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit")
                    }

                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun WorkEntryDetailCard(
    workDetails: WorkWithDetails,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val work = workDetails.work
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(work.workName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Customer: ${workDetails.customer.name}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "₹${"%.2f".format(work.totalWorkAmount)}",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = ProfitGreen
                    )

                    if (workDetails.customer.mobileNumber.isNotBlank()) {
                        IconButton(
                            onClick = {
                                CustomerShareUtils.openWhatsApp(
                                    context = context,
                                    mobileNumber = workDetails.customer.mobileNumber,
                                    customerName = workDetails.customer.name,
                                    workName = work.workName,
                                    dateMillis = work.date,
                                    totalAmount = work.totalWorkAmount,
                                    pendingAmount = workDetails.customer.pendingBalance
                                )
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.ChatBubble, contentDescription = "WhatsApp Bill", tint = ProfitGreen, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = {
                                CustomerShareUtils.openSms(
                                    context = context,
                                    mobileNumber = workDetails.customer.mobileNumber,
                                    customerName = workDetails.customer.name,
                                    workName = work.workName,
                                    dateMillis = work.date,
                                    totalAmount = work.totalWorkAmount,
                                    pendingAmount = workDetails.customer.pendingBalance
                                )
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = "SMS Bill", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Vehicle: ${workDetails.vehicle.vehicleName} (${workDetails.vehicle.vehicleNumber})", style = MaterialTheme.typography.bodySmall)
                Text("Driver: ${workDetails.driver.driverName}", style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hours: ${work.totalTimeHours}h @ ₹${work.hourlyRate}/h", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                if (work.location.isNotBlank()) {
                    Text("📍 ${work.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun WorkDetailDialog(
    workDetails: WorkWithDetails,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val work = workDetails.work
    val dateStr = remember(work.date) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(work.date))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(work.workName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Customer details card with contact actions
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Customer / Client:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(workDetails.customer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                if (workDetails.customer.mobileNumber.isNotBlank()) {
                                    Text(workDetails.customer.mobileNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            if (workDetails.customer.mobileNumber.isNotBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { CustomerShareUtils.makeCall(context, workDetails.customer.mobileNumber) }) {
                                        Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = {
                                        CustomerShareUtils.openWhatsApp(
                                            context = context,
                                            mobileNumber = workDetails.customer.mobileNumber,
                                            customerName = workDetails.customer.name,
                                            workName = work.workName,
                                            dateMillis = work.date,
                                            totalAmount = work.totalWorkAmount,
                                            pendingAmount = workDetails.customer.pendingBalance
                                        )
                                    }) {
                                        Icon(Icons.Default.ChatBubble, contentDescription = "WhatsApp", tint = ProfitGreen)
                                    }
                                }
                            }
                        }
                    }
                }

                // Work specifics
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Vehicle:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${workDetails.vehicle.vehicleName} (${workDetails.vehicle.vehicleNumber})", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Operating Driver:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(workDetails.driver.driverName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pricing Mode:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (work.hourlyRate > 0) "Hourly (${work.totalTimeHours}h @ ₹${work.hourlyRate}/h)" else "Fixed Amount", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        }
                        if (work.location.isNotBlank()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Site Location:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(work.location, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Financial Summary
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Billed Amount:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("₹${"%.2f".format(work.totalWorkAmount)}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyMedium, color = ProfitGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Customer Balance Pending:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${"%.2f".format(workDetails.customer.pendingBalance)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = if (workDetails.customer.pendingBalance > 0) ExpenseRed else ProfitGreen)
                        }
                    }
                }

                HorizontalDivider()

                // Actions Inside Card: Edit and Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit")
                    }

                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleFormDialog(
    vehicleToEdit: VehicleEntity?,
    viewModel: BalajiViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(vehicleToEdit?.vehicleName ?: "") }
    var number by remember { mutableStateOf(vehicleToEdit?.vehicleNumber ?: "") }
    var model by remember { mutableStateOf(vehicleToEdit?.model ?: "") }
    var workedHours by remember { mutableStateOf(vehicleToEdit?.workedHours?.toString() ?: "0.0") }
    var imageUri by remember { mutableStateOf(vehicleToEdit?.imageUri) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            imageUri = it.toString()
        }
    }

    val vehicleTypePresets = listOf(
        "JCB 3DX",
        "Tractor",
        "Tractor with Trolley",
        "Dumper / Tipper",
        "Excavator (Poclain)",
        "Mini Excavator",
        "Compactor / Roller",
        "Hydra / Crane",
        "Water Tanker",
        "Other"
    )
    var isOtherSelected by remember {
        mutableStateOf(vehicleToEdit != null && vehicleToEdit.vehicleName !in vehicleTypePresets.dropLast(1))
    }
    var customVehicleName by remember {
        mutableStateOf(if (isOtherSelected) vehicleToEdit?.vehicleName ?: "" else "")
    }
    var nameDropdownExpanded by remember { mutableStateOf(false) }

    val districtCodePresets = listOf(
        "MH-09" to "Kolhapur",
        "MH-12" to "Pune",
        "MH-14" to "Pimpri-Chinchwad",
        "MH-10" to "Sangli",
        "MH-11" to "Satara",
        "MH-13" to "Solapur",
        "MH-08" to "Ratnagiri",
        "MH-20" to "Chh. Sambhajinagar",
        "MH-05" to "Kalyan",
        "MH-06" to "Mumbai",
        "MH-45" to "Akluj",
        "MH-50" to "Karad",
        "KA-22" to "Belgaum",
        "KA-28" to "Bijapur",
        "GA-01" to "Goa North",
        "GJ-01" to "Ahmedabad"
    )
    var codeDropdownExpanded by remember { mutableStateOf(false) }
    var showOcrScanner by remember { mutableStateOf(false) }

    if (showOcrScanner) {
        DocumentScanDialog(
            targetType = "Vehicle",
            onDismiss = { showOcrScanner = false },
            onDataExtracted = { scanned ->
                if (scanned.vehicleNumber.isNotBlank()) {
                    number = scanned.vehicleNumber
                }
                if (scanned.name.isNotBlank()) {
                    val matchedPreset = vehicleTypePresets.find { it.contains(scanned.name, ignoreCase = true) }
                    if (matchedPreset != null) {
                        name = matchedPreset
                        isOtherSelected = false
                    } else {
                        isOtherSelected = true
                        customVehicleName = scanned.name
                        name = scanned.name
                    }
                }
                if (scanned.hours.isNotBlank()) {
                    workedHours = scanned.hours
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .imePadding(),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (vehicleToEdit == null) "Add Vehicle" else "Edit Vehicle", fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = { showOcrScanner = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan RC", fontSize = 11.sp)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Vehicle Photo Picker Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EntityAvatar(
                        imageUri = imageUri,
                        fallbackIcon = Icons.Default.DirectionsCar,
                        contentDescription = "Vehicle Photo",
                        size = 56.dp
                    )
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (imageUri != null) "Change Photo" else "Upload Photo (वाहन फोटो)", fontSize = 12.sp)
                    }
                }

                // Vehicle Name with preset dropdown
                ExposedDropdownMenuBox(
                    expanded = nameDropdownExpanded,
                    onExpandedChange = { nameDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Vehicle Type / Name *") },
                        placeholder = { Text("Select from list or type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nameDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = nameDropdownExpanded,
                        onDismissRequest = { nameDropdownExpanded = false }
                    ) {
                        vehicleTypePresets.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    if (item == "Other") {
                                        isOtherSelected = true
                                        name = customVehicleName
                                    } else {
                                        isOtherSelected = false
                                        name = item
                                    }
                                    nameDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (isOtherSelected) {
                    OutlinedTextField(
                        value = customVehicleName,
                        onValueChange = {
                            customVehicleName = it
                            name = it
                        },
                        label = { Text("Custom Vehicle Name *") },
                        placeholder = { Text("e.g. Grader, Bulldozer, Bobcat") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Vehicle Number with RTO State & District dropdown
                ExposedDropdownMenuBox(
                    expanded = codeDropdownExpanded,
                    onExpandedChange = { codeDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = number,
                        onValueChange = { number = it.uppercase() },
                        label = { Text("Vehicle Number *") },
                        placeholder = { Text("e.g. MH-09-FJ-8359") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = codeDropdownExpanded) },
                        supportingText = { Text("Select state/district RTO code or type directly") },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = codeDropdownExpanded,
                        onDismissRequest = { codeDropdownExpanded = false }
                    ) {
                        districtCodePresets.forEach { (code, district) ->
                            DropdownMenuItem(
                                text = { Text("$code ($district)") },
                                onClick = {
                                    val currentText = number.trim()
                                    val parts = currentText.split("-")
                                    val suffix = if (parts.size >= 3) {
                                        parts.drop(2).joinToString("-")
                                    } else ""
                                    number = if (suffix.isNotBlank()) "$code-$suffix" else "$code-"
                                    codeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model / Specs (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = workedHours,
                    onValueChange = { workedHours = it },
                    label = { Text("Starting Worked Hours") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hrs = workedHours.toDoubleOrNull() ?: 0.0
                    if (vehicleToEdit == null) {
                        val newV = VehicleEntity(
                            vehicleName = name.trim(),
                            vehicleNumber = number.trim().uppercase(),
                            model = model.trim(),
                            workedHours = hrs,
                            lastServiceHours = hrs,
                            purchaseDate = System.currentTimeMillis(),
                            imageUri = imageUri
                        )
                        viewModel.submitVehicle(newV) { onDismiss() }
                    } else {
                        val updated = vehicleToEdit.copy(
                            vehicleName = name.trim(),
                            vehicleNumber = number.trim().uppercase(),
                            model = model.trim(),
                            workedHours = hrs,
                            imageUri = imageUri
                        )
                        viewModel.editVehicle(updated) { onDismiss() }
                    }
                },
                enabled = name.isNotBlank() && number.isNotBlank()
            ) {
                Text(if (vehicleToEdit == null) "Save Vehicle" else "Update Vehicle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkDialog(
    workDetails: WorkWithDetails,
    viewModel: BalajiViewModel,
    onDismiss: () -> Unit
) {
    val work = workDetails.work
    var workName by remember { mutableStateOf(work.workName) }
    var isFixedAmount by remember { mutableStateOf(work.totalTimeHours <= 0.0 || work.hourlyRate <= 0.0) }
    var fixedAmount by remember { mutableStateOf(work.totalWorkAmount.toString()) }
    var totalHours by remember { mutableStateOf(if (work.totalTimeHours > 0.0) work.totalTimeHours.toString() else "") }
    var hourlyRate by remember { mutableStateOf(if (work.hourlyRate > 0.0) work.hourlyRate.toString() else "") }
    var finalSettledAmount by remember { mutableStateOf(work.finalSettledAmount?.toString() ?: "") }
    var location by remember { mutableStateOf(work.location) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .imePadding(),
        title = { Text("Edit Work", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = workName,
                    onValueChange = { workName = it },
                    label = { Text("Work Title *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Customer: ${workDetails.customer.name}\nVehicle: ${workDetails.vehicle.vehicleName} | Driver: ${workDetails.driver.driverName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isFixedAmount,
                        onClick = { isFixedAmount = false },
                        label = { Text("Hourly Work") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isFixedAmount,
                        onClick = { isFixedAmount = true },
                        label = { Text("Fixed Amount") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isFixedAmount) {
                    OutlinedTextField(
                        value = fixedAmount,
                        onValueChange = { fixedAmount = it },
                        label = { Text("Fixed Work Amount (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = totalHours,
                        onValueChange = { totalHours = it },
                        label = { Text("Hours Worked (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = totalHours,
                            onValueChange = { totalHours = it },
                            label = { Text("Hours *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = hourlyRate,
                            onValueChange = { hourlyRate = it },
                            label = { Text("Rate (₹/h) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                val calcAmount = if (isFixedAmount) {
                    fixedAmount.toDoubleOrNull() ?: 0.0
                } else {
                    val h = totalHours.toDoubleOrNull() ?: 0.0
                    val r = hourlyRate.toDoubleOrNull() ?: 0.0
                    h * r
                }

                OutlinedTextField(
                    value = finalSettledAmount,
                    onValueChange = { finalSettledAmount = it },
                    label = { Text("Final Settled Amount (₹) / ठरलेली रक्कम") },
                    placeholder = { Text("Calculated: ₹${"%.0f".format(calcAmount)} (Enter if bargained)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                val effectiveAmount = finalSettledAmount.toDoubleOrNull() ?: calcAmount

                Text(
                    "Billed Total: ₹${"%.2f".format(effectiveAmount)}",
                    fontWeight = FontWeight.Bold,
                    color = ProfitGreen
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Site Location") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val h = totalHours.toDoubleOrNull() ?: 0.0
                    val r = hourlyRate.toDoubleOrNull() ?: 0.0
                    val calc = if (isFixedAmount) (fixedAmount.toDoubleOrNull() ?: work.totalWorkAmount) else (h * r)
                    val finalVal = finalSettledAmount.toDoubleOrNull()
                    val effective = finalVal ?: calc
                    val updated = work.copy(
                        workName = workName.trim(),
                        totalTimeHours = h,
                        hourlyRate = if (isFixedAmount) 0.0 else r,
                        totalWorkAmount = effective,
                        finalSettledAmount = finalVal,
                        location = location.trim()
                    )
                    viewModel.editWork(updated) { onDismiss() }
                },
                enabled = workName.isNotBlank() && (if (isFixedAmount) (fixedAmount.toDoubleOrNull() ?: 0.0) > 0.0 else (totalHours.toDoubleOrNull() ?: 0.0) > 0.0)
            ) {
                Text("Update Work")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
