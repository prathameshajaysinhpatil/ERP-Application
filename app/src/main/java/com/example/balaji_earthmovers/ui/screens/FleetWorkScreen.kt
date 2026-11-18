package com.example.balaji_earthmovers.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.balaji_earthmovers.data.local.entity.VehicleEntity
import com.example.balaji_earthmovers.data.local.entity.WorkEntity
import com.example.balaji_earthmovers.data.local.entity.WorkWithDetails
import com.example.balaji_earthmovers.ui.theme.AmberPrimary
import com.example.balaji_earthmovers.ui.theme.ExpenseRed
import com.example.balaji_earthmovers.ui.theme.InfoBlue
import com.example.balaji_earthmovers.ui.theme.ProfitGreen
import com.example.balaji_earthmovers.ui.util.LocalStrings
import com.example.balaji_earthmovers.ui.viewmodel.BalajiViewModel
import com.example.balaji_earthmovers.ui.viewmodel.FleetSeasonFilter

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

    var workToEditPending by remember { mutableStateOf<WorkWithDetails?>(null) }
    var workToEdit by remember { mutableStateOf<WorkWithDetails?>(null) }
    var workToDelete by remember { mutableStateOf<WorkWithDetails?>(null) }

    val vehicles by viewModel.filteredVehicles.collectAsState()
    val works by viewModel.allWorks.collectAsState()
    val seasonFilter by viewModel.seasonFilter.collectAsState()

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
                    text = { Text("${strings.tabWork} (${works.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedTab == 0) {
                // Seasonal Filter Row
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
                                onEdit = { vehicleToEditPending = vehicle },
                                onDelete = { vehicleToDelete = vehicle },
                                onMarkServiced = { viewModel.markVehicleServiced(vehicle.id) }
                            )
                        }
                    }
                }
            } else {
                // Work Entries List
                if (works.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No work entries logged. Tap '+' to add work.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(works, key = { it.work.id }) { workDetails ->
                            WorkEntryDetailCard(
                                workDetails = workDetails,
                                onEdit = { workToEditPending = workDetails },
                                onDelete = { workToDelete = workDetails }
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
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkServiced: () -> Unit
) {
    val isOverdue = vehicle.isServiceDue
    val hoursSinceService = vehicle.hoursSinceLastService
    val progress = (hoursSinceService / 250.0).coerceIn(0.0, 1.0).toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(vehicle.vehicleName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Number: ${vehicle.vehicleNumber} • Model: ${vehicle.model}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Vehicle", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Vehicle", tint = ExpenseRed)
                    }
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

            // Action: Reset 250h service button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onMarkServiced,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Service", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun WorkEntryDetailCard(
    workDetails: WorkWithDetails,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val work = workDetails.work
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Work", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Work", tint = ExpenseRed)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleFormDialog(
    vehicleToEdit: VehicleEntity?,
    viewModel: BalajiViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(vehicleToEdit?.vehicleName ?: "") }
    var number by remember { mutableStateOf(vehicleToEdit?.vehicleNumber ?: "") }
    var model by remember { mutableStateOf(vehicleToEdit?.model ?: "") }
    var workedHours by remember { mutableStateOf(vehicleToEdit?.workedHours?.toString() ?: "0.0") }

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vehicleToEdit == null) "Add Vehicle" else "Edit Vehicle", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                            purchaseDate = System.currentTimeMillis()
                        )
                        viewModel.submitVehicle(newV) { onDismiss() }
                    } else {
                        val updated = vehicleToEdit.copy(
                            vehicleName = name.trim(),
                            vehicleNumber = number.trim().uppercase(),
                            model = model.trim(),
                            workedHours = hrs
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
    var totalHours by remember { mutableStateOf(work.totalTimeHours.toString()) }
    var hourlyRate by remember { mutableStateOf(work.hourlyRate.toString()) }
    var location by remember { mutableStateOf(work.location) }

    AlertDialog(
        onDismissRequest = onDismiss,
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

                val h = totalHours.toDoubleOrNull() ?: 0.0
                val r = hourlyRate.toDoubleOrNull() ?: 0.0
                Text(
                    "Calculated Amount: ₹${"%.2f".format(h * r)}",
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
                    val h = totalHours.toDoubleOrNull() ?: work.totalTimeHours
                    val r = hourlyRate.toDoubleOrNull() ?: work.hourlyRate
                    val updated = work.copy(
                        workName = workName.trim(),
                        totalTimeHours = h,
                        hourlyRate = r,
                        totalWorkAmount = h * r,
                        location = location.trim()
                    )
                    viewModel.editWork(updated) { onDismiss() }
                },
                enabled = workName.isNotBlank() && (totalHours.toDoubleOrNull() ?: 0.0) > 0.0
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
