package com.example.balaji_earthmovers.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.balaji_earthmovers.data.local.entity.PaymentMode
import com.example.balaji_earthmovers.ui.theme.ExpenseRed
import com.example.balaji_earthmovers.ui.theme.ProfitGreen
import com.example.balaji_earthmovers.ui.viewmodel.BalajiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkEntryDialog(
    viewModel: BalajiViewModel,
    onDismiss: () -> Unit
) {
    val formState by viewModel.workFormState.collectAsState()
    val vehicles by viewModel.allVehicles.collectAsState()
    val drivers by viewModel.allDrivers.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()

    var vehicleExpanded by remember { mutableStateOf(false) }
    var driverExpanded by remember { mutableStateOf(false) }
    var customerExpanded by remember { mutableStateOf(false) }
    var paymentModeExpanded by remember { mutableStateOf(false) }

    var showOcrScanner by remember { mutableStateOf(false) }

    if (showOcrScanner) {
        DocumentScanDialog(
            targetType = "Work",
            onDismiss = { showOcrScanner = false },
            onDataExtracted = { scanned ->
                viewModel.updateWorkForm { s ->
                    // Match vehicle from scanned vehicleNumber or text
                    val matchedVehicle = vehicles.find { v ->
                        scanned.vehicleNumber.isNotBlank() &&
                        (v.vehicleNumber.contains(scanned.vehicleNumber, ignoreCase = true) ||
                         scanned.vehicleNumber.contains(v.vehicleNumber.replace("-", ""), ignoreCase = true))
                    }
                    val vehicleId = matchedVehicle?.id ?: s.selectedVehicleId
                    val driverId = matchedVehicle?.currentDriverId ?: s.selectedDriverId

                    // Match customer from scanned name
                    val matchedCust = customers.find { c ->
                        (scanned.name.isNotBlank() && c.name.contains(scanned.name, ignoreCase = true)) ||
                        (scanned.mobileNumber.isNotBlank() && c.mobileNumber == scanned.mobileNumber)
                    }
                    val custId = matchedCust?.id ?: s.selectedCustomerId

                    s.copy(
                        workName = if (scanned.name.isNotBlank()) "Site - ${scanned.name}" else s.workName,
                        selectedVehicleId = vehicleId,
                        selectedDriverId = driverId,
                        selectedCustomerId = custId,
                        totalHours = if (scanned.hours.isNotBlank()) scanned.hours else s.totalHours,
                        hourlyRate = if (scanned.rate.isNotBlank()) scanned.rate else s.hourlyRate,
                        fixedAmount = if (scanned.amount.isNotBlank()) scanned.amount else s.fixedAmount,
                        initialPaidAmount = if (scanned.amount.isNotBlank() && s.isFixedAmount) scanned.amount else s.initialPaidAmount
                    )
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
                Text("Add Work Entry", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                OutlinedButton(
                    onClick = { showOcrScanner = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan Slip", fontSize = 11.sp)
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
                Text(
                    "Assign vehicle, driver & customer. Enter hours/rate or fixed amount and the exact payment received.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Work Name
                OutlinedTextField(
                    value = formState.workName,
                    onValueChange = { viewModel.updateWorkForm { s -> s.copy(workName = it) } },
                    label = { Text("Work Title / Site Name *") },
                    isError = formState.workNameError != null,
                    supportingText = formState.workNameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Vehicle Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = vehicleExpanded,
                    onExpandedChange = { vehicleExpanded = it }
                ) {
                    val selectedVehicle = vehicles.find { it.id == formState.selectedVehicleId }
                    OutlinedTextField(
                        value = selectedVehicle?.let { "${it.vehicleName} (${it.vehicleNumber})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Vehicle *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleExpanded) },
                        leadingIcon = { Icon(Icons.Default.DriveEta, contentDescription = null) },
                        isError = formState.vehicleError != null,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = vehicleExpanded,
                        onDismissRequest = { vehicleExpanded = false }
                    ) {
                        vehicles.forEach { vehicle ->
                            DropdownMenuItem(
                                text = { Text("${vehicle.vehicleName} - ${vehicle.vehicleNumber}") },
                                onClick = {
                                    viewModel.updateWorkForm { s ->
                                        s.copy(
                                            selectedVehicleId = vehicle.id,
                                            vehicleError = null,
                                            selectedDriverId = vehicle.currentDriverId ?: s.selectedDriverId,
                                            driverError = if (vehicle.currentDriverId != null) null else s.driverError
                                        )
                                    }
                                    vehicleExpanded = false
                                }
                            )
                        }
                    }
                }

                // Driver Selector Dropdown (auto-assigned from vehicle)
                ExposedDropdownMenuBox(
                    expanded = driverExpanded,
                    onExpandedChange = { driverExpanded = it }
                ) {
                    val selectedDriver = drivers.find { it.id == formState.selectedDriverId }
                    OutlinedTextField(
                        value = selectedDriver?.let { "${it.driverName} (${it.mobileNumber})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Operating Driver *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = driverExpanded) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        isError = formState.driverError != null,
                        supportingText = formState.driverError?.let { { Text(it) } },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = driverExpanded,
                        onDismissRequest = { driverExpanded = false }
                    ) {
                        drivers.forEach { driver ->
                            DropdownMenuItem(
                                text = { Text("${driver.driverName} (${driver.mobileNumber})") },
                                onClick = {
                                    viewModel.updateWorkForm { s -> s.copy(selectedDriverId = driver.id, driverError = null) }
                                    driverExpanded = false
                                }
                            )
                        }
                    }
                }

                // Customer Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = customerExpanded,
                    onExpandedChange = { customerExpanded = it }
                ) {
                    val selectedCustomer = customers.find { it.id == formState.selectedCustomerId }
                    OutlinedTextField(
                        value = selectedCustomer?.let { "${it.name} (${it.mobileNumber})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Customer / Client *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerExpanded) },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = customerExpanded,
                        onDismissRequest = { customerExpanded = false }
                    ) {
                        customers.forEach { customer ->
                            DropdownMenuItem(
                                text = { Text("${customer.name} (${customer.mobileNumber})") },
                                onClick = {
                                    viewModel.updateWorkForm { s -> s.copy(selectedCustomerId = customer.id, customerError = null) }
                                    customerExpanded = false
                                }
                            )
                        }
                    }
                }

                // Pricing Option: Hourly Work vs Fixed Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !formState.isFixedAmount,
                        onClick = { viewModel.updateWorkForm { s -> s.copy(isFixedAmount = false) } },
                        label = { Text("Hourly Work") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = formState.isFixedAmount,
                        onClick = { viewModel.updateWorkForm { s -> s.copy(isFixedAmount = true) } },
                        label = { Text("Fixed Amount") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (formState.isFixedAmount) {
                    // Fixed Amount Input
                    OutlinedTextField(
                        value = formState.fixedAmount,
                        onValueChange = { viewModel.updateWorkForm { s -> s.copy(fixedAmount = it) } },
                        label = { Text("Fixed Work Amount (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = formState.fixedAmountError != null,
                        supportingText = formState.fixedAmountError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Optional Hours Input
                    OutlinedTextField(
                        value = formState.totalHours,
                        onValueChange = { viewModel.updateWorkForm { s -> s.copy(totalHours = it) } },
                        label = { Text("Hours Worked (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Hours & Rate Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = formState.totalHours,
                            onValueChange = { viewModel.updateWorkForm { s -> s.copy(totalHours = it) } },
                            label = { Text("Hours *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = formState.totalHoursError != null,
                            supportingText = formState.totalHoursError?.let { { Text(it) } },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = formState.hourlyRate,
                            onValueChange = { viewModel.updateWorkForm { s -> s.copy(hourlyRate = it) } },
                            label = { Text("Rate (₹/hr) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = formState.hourlyRateError != null,
                            supportingText = formState.hourlyRateError?.let { { Text(it) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ==================== WORK PAYMENT (EXACT AMOUNT PAID & FINAL SETTLED AMOUNT) ====================
                // Prominently placed directly below both Fixed & Hourly payment modes
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Work Payment & Final Settlement:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Final Negotiated / Settled Amount Field (Bargain/Discounted Total)
                        OutlinedTextField(
                            value = formState.finalSettledAmount,
                            onValueChange = { viewModel.updateWorkForm { s -> s.copy(finalSettledAmount = it) } },
                            label = { Text("Final Settled Amount (₹) / ठरलेली अंतिम रक्कम") },
                            placeholder = { Text("Calculated: ₹${"%.0f".format(formState.totalAmount)} (Enter if bargained)") },
                            supportingText = {
                                Text(
                                    if (formState.finalSettledAmount.isNotBlank())
                                        "Negotiated final amount will be billed instead of ₹${"%.0f".format(formState.totalAmount)}"
                                    else
                                        "Leave blank to use calculated amount (₹${"%.0f".format(formState.totalAmount)})",
                                    fontSize = 11.sp
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = formState.initialPaidAmount,
                                onValueChange = { viewModel.updateWorkForm { s -> s.copy(initialPaidAmount = it) } },
                                label = { Text("Amount Paid (₹) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1.2f)
                            )

                            // Payment Mode Dropdown
                            ExposedDropdownMenuBox(
                                expanded = paymentModeExpanded,
                                onExpandedChange = { paymentModeExpanded = it },
                                modifier = Modifier.weight(0.9f)
                            ) {
                                OutlinedTextField(
                                    value = formState.paymentMode.name,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Mode") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentModeExpanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = paymentModeExpanded,
                                    onDismissRequest = { paymentModeExpanded = false }
                                ) {
                                    PaymentMode.values().forEach { mode ->
                                        DropdownMenuItem(
                                            text = { Text(mode.name) },
                                            onClick = {
                                                viewModel.updateWorkForm { s -> s.copy(paymentMode = mode) }
                                                paymentModeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Real-time Calculated Pending Balance using Effective Billed Amount
                        val calcTotal = formState.effectiveBilledAmount
                        val paid = formState.initialPaidAmount.toDoubleOrNull() ?: 0.0
                        val pending = (calcTotal - paid).coerceAtLeast(0.0)

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (pending > 0.0) ExpenseRed.copy(alpha = 0.08f) else ProfitGreen.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Billed Amount", style = MaterialTheme.typography.labelSmall)
                                    Text("₹${"%.2f".format(calcTotal)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Paid", style = MaterialTheme.typography.labelSmall)
                                    Text("₹${"%.2f".format(paid)}", fontWeight = FontWeight.Bold, color = ProfitGreen, fontSize = 13.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Remaining Pending", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        "₹${"%.2f".format(pending)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (pending > 0.0) ExpenseRed else ProfitGreen,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        if (paid > 0.0) {
                            OutlinedTextField(
                                value = formState.transactionRef,
                                onValueChange = { viewModel.updateWorkForm { s -> s.copy(transactionRef = it) } },
                                label = { Text("Reference ID / UTR / Cheque No.") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Location / GPS
                OutlinedTextField(
                    value = formState.location,
                    onValueChange = { viewModel.updateWorkForm { s -> s.copy(location = it) } },
                    label = { Text("Site Location (Text / Coordinates)") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.submitWorkEntry {
                        onDismiss()
                    }
                },
                enabled = formState.isValid
            ) {
                Text("Save Work")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
