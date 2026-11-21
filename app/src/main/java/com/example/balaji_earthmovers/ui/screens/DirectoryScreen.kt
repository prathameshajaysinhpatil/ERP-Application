package com.example.balaji_earthmovers.ui.screens

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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.balaji_earthmovers.data.local.entity.CustomerEntity
import com.example.balaji_earthmovers.data.local.entity.DriverEntity
import com.example.balaji_earthmovers.data.security.CryptoManager
import com.example.balaji_earthmovers.ui.theme.ExpenseRed
import com.example.balaji_earthmovers.ui.theme.ProfitGreen
import com.example.balaji_earthmovers.ui.util.CustomerShareUtils
import com.example.balaji_earthmovers.ui.util.LocalStrings
import com.example.balaji_earthmovers.ui.viewmodel.BalajiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryScreen(
    viewModel: BalajiViewModel,
    onSelectDriverPassbook: (DriverEntity) -> Unit,
    onSelectCustomerPassbook: (CustomerEntity) -> Unit = {},
    onNavigateToAging: () -> Unit,
    initialTab: Int = 0
) {
    val strings = LocalStrings.current
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    var showAddDriverDialog by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }

    var driverToEditPending by remember { mutableStateOf<DriverEntity?>(null) }
    var driverToEdit by remember { mutableStateOf<DriverEntity?>(null) }
    var driverToDelete by remember { mutableStateOf<DriverEntity?>(null) }

    var customerToEditPending by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    val drivers by viewModel.allDrivers.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddDriverDialog = true else showAddCustomerDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = if (selectedTab == 0) strings.addDriver else strings.addCustomer
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
                    text = { Text("${strings.tabDriver} (${drivers.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("${strings.tabCustomer} (${customers.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedTab == 0) {
                // Driver Tab
                if (drivers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No drivers registered. Tap '+' to add driver.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(drivers, key = { it.id }) { driver ->
                            DriverDirectoryCard(
                                driver = driver,
                                onOpenPassbook = { onSelectDriverPassbook(driver) },
                                onEdit = { driverToEditPending = driver },
                                onDelete = { driverToDelete = driver }
                            )
                        }
                    }
                }
            } else {
                // Customer Tab
                if (customers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No customers registered. Tap '+' to add customer.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(customers, key = { it.id }) { customer ->
                            CustomerDirectoryCard(
                                customer = customer,
                                onOpenPassbook = { onSelectCustomerPassbook(customer) },
                                onEdit = { customerToEditPending = customer },
                                onDelete = { customerToDelete = customer }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDriverDialog) {
        AddDriverDialog(
            viewModel = viewModel,
            onDismiss = { showAddDriverDialog = false }
        )
    }

    driverToEditPending?.let { driver ->
        AdminPasswordEditDialog(
            itemType = "Driver",
            itemName = driver.driverName,
            viewModel = viewModel,
            onDismiss = { driverToEditPending = null },
            onVerified = {
                driverToEdit = driver
                driverToEditPending = null
            }
        )
    }

    driverToEdit?.let { driver ->
        EditDriverDialog(
            driver = driver,
            viewModel = viewModel,
            onDismiss = { driverToEdit = null }
        )
    }

    driverToDelete?.let { driver ->
        AdminPasswordDeleteDialog(
            itemType = "Driver",
            itemName = driver.driverName,
            viewModel = viewModel,
            onDismiss = { driverToDelete = null },
            onConfirmDelete = {
                viewModel.deleteDriver(driver.id)
            }
        )
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            viewModel = viewModel,
            onDismiss = { showAddCustomerDialog = false }
        )
    }

    customerToEditPending?.let { customer ->
        AdminPasswordEditDialog(
            itemType = "Customer",
            itemName = customer.name,
            viewModel = viewModel,
            onDismiss = { customerToEditPending = null },
            onVerified = {
                customerToEdit = customer
                customerToEditPending = null
            }
        )
    }

    customerToEdit?.let { customer ->
        EditCustomerDialog(
            customer = customer,
            viewModel = viewModel,
            onDismiss = { customerToEdit = null }
        )
    }

    customerToDelete?.let { customer ->
        AdminPasswordDeleteDialog(
            itemType = "Customer",
            itemName = customer.name,
            viewModel = viewModel,
            onDismiss = { customerToDelete = null },
            onConfirmDelete = {
                viewModel.deleteCustomer(customer.id)
            }
        )
    }
}

@Composable
fun DriverDirectoryCard(
    driver: DriverEntity,
    onOpenPassbook: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val maskedAadhaar = remember(driver.adharNumber) {
        val decrypted = CryptoManager.decrypt(driver.adharNumber)
        CryptoManager.maskAadhaar(decrypted)
    }

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
                    Text(driver.driverName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(driver.mobileNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (driver.address.isNotBlank()) {
                        Text(driver.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = ProfitGreen)
                        Text("Aadhaar: $maskedAadhaar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Driver", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Driver", tint = ExpenseRed)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (driver.pendingSalary > 0) MaterialTheme.colorScheme.primaryContainer else ProfitGreen.copy(alpha = 0.12f),
                        contentColor = if (driver.pendingSalary > 0) MaterialTheme.colorScheme.onPrimaryContainer else ProfitGreen
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("Pending", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "₹${"%.0f".format(driver.pendingSalary)}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Monthly: ₹${"%.0f".format(driver.monthlySalary)} | Paid: ₹${"%.0f".format(driver.givenSalary)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onOpenPassbook,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Passbook", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun CustomerDirectoryCard(
    customer: CustomerEntity,
    onOpenPassbook: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
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
                    Text(customer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(customer.mobileNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (customer.mobileNumber.isNotBlank()) {
                            IconButton(
                                onClick = { CustomerShareUtils.makeCall(context, customer.mobileNumber) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                            }
                            IconButton(
                                onClick = {
                                    CustomerShareUtils.openWhatsApp(
                                        context = context,
                                        mobileNumber = customer.mobileNumber,
                                        customerName = customer.name,
                                        totalAmount = customer.totalBilled,
                                        pendingAmount = customer.pendingBalance
                                    )
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.ChatBubble, contentDescription = "WhatsApp", tint = ProfitGreen, modifier = Modifier.size(15.dp))
                            }
                            IconButton(
                                onClick = {
                                    CustomerShareUtils.openSms(
                                        context = context,
                                        mobileNumber = customer.mobileNumber,
                                        customerName = customer.name,
                                        totalAmount = customer.totalBilled,
                                        pendingAmount = customer.pendingBalance
                                    )
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.Sms, contentDescription = "SMS", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                    if (customer.gstin.isNotBlank()) {
                        Text("GSTIN: ${customer.gstin}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Customer", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Customer", tint = ExpenseRed)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (customer.pendingBalance > 0) ExpenseRed.copy(alpha = 0.12f) else ProfitGreen.copy(alpha = 0.12f),
                        contentColor = if (customer.pendingBalance > 0) ExpenseRed else ProfitGreen
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("Receivable", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "₹${"%.2f".format(customer.pendingBalance)}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Billed: ₹${"%.0f".format(customer.totalBilled)} | Paid: ₹${"%.0f".format(customer.totalPaid)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onOpenPassbook,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Passbook", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDriverDialog(
    driver: DriverEntity,
    viewModel: BalajiViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(driver.driverName) }
    var mobile by remember { mutableStateOf(driver.mobileNumber) }
    var address by remember { mutableStateOf(driver.address) }
    val initialAadhaar = remember(driver.adharNumber) {
        CryptoManager.decrypt(driver.adharNumber)
    }
    var aadhaar by remember { mutableStateOf(initialAadhaar) }
    var monthlySalary by remember { mutableStateOf(driver.monthlySalary.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .imePadding(),
        title = { Text("Edit Driver", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Driver Full Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number (10 Digits) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Residential Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = aadhaar,
                    onValueChange = { aadhaar = it },
                    label = { Text("Aadhaar Number (12 Digits)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = monthlySalary,
                    onValueChange = { monthlySalary = it },
                    label = { Text("Monthly Salary (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val salary = monthlySalary.toDoubleOrNull() ?: driver.monthlySalary
                    val updated = driver.copy(
                        driverName = name.trim(),
                        mobileNumber = mobile.trim(),
                        address = address.trim(),
                        adharNumber = aadhaar.trim(),
                        monthlySalary = salary
                    )
                    viewModel.editDriver(updated) {
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank() && mobile.isNotBlank()
            ) {
                Text("Update Driver")
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
fun EditCustomerDialog(
    customer: CustomerEntity,
    viewModel: BalajiViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(customer.name) }
    var mobile by remember { mutableStateOf(customer.mobileNumber) }
    var address by remember { mutableStateOf(customer.address) }
    var gstin by remember { mutableStateOf(customer.gstin) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .imePadding(),
        title = { Text("Edit Customer", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer / Firm Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Contact Mobile (10 Digits) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Billing Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = gstin,
                    onValueChange = { gstin = it },
                    label = { Text("GSTIN (Optional 15 chars)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = customer.copy(
                        name = name.trim(),
                        mobileNumber = mobile.trim(),
                        address = address.trim(),
                        gstin = gstin.trim().uppercase()
                    )
                    viewModel.editCustomer(updated) {
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank() && mobile.isNotBlank()
            ) {
                Text("Update Customer")
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
fun AddDriverDialog(
    viewModel: BalajiViewModel,
    onDismiss: () -> Unit
) {
    val formState by viewModel.driverFormState.collectAsState()
    var showOcrScanner by remember { mutableStateOf(false) }

    if (showOcrScanner) {
        DocumentScanDialog(
            targetType = "Driver",
            onDismiss = { showOcrScanner = false },
            onDataExtracted = { scanned ->
                viewModel.updateDriverForm { s ->
                    s.copy(
                        name = if (scanned.name.isNotBlank()) scanned.name else s.name,
                        mobile = if (scanned.mobileNumber.isNotBlank()) scanned.mobileNumber else s.mobile,
                        aadhaar = if (scanned.aadhaarNumber.isNotBlank()) scanned.aadhaarNumber else s.aadhaar
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
                Text("Add Driver", fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = { showOcrScanner = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan Aadhaar", fontSize = 11.sp)
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
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { viewModel.updateDriverForm { s -> s.copy(name = it) } },
                    label = { Text("Driver Full Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.mobile,
                    onValueChange = { viewModel.updateDriverForm { s -> s.copy(mobile = it) } },
                    label = { Text("Mobile Number (10 Digits) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = formState.mobileError != null,
                    supportingText = formState.mobileError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.address,
                    onValueChange = { viewModel.updateDriverForm { s -> s.copy(address = it) } },
                    label = { Text("Residential Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.aadhaar,
                    onValueChange = { viewModel.updateDriverForm { s -> s.copy(aadhaar = it) } },
                    label = { Text("Aadhaar Number (12 Digits - Encrypted)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = formState.aadhaarError != null,
                    supportingText = formState.aadhaarError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.monthlySalary,
                    onValueChange = { viewModel.updateDriverForm { s -> s.copy(monthlySalary = it) } },
                    label = { Text("Monthly Salary (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = formState.salaryError != null,
                    supportingText = formState.salaryError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.submitDriver {
                        onDismiss()
                    }
                },
                enabled = formState.isValid
            ) {
                Text("Save Driver")
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
fun AddCustomerDialog(
    viewModel: BalajiViewModel,
    onDismiss: () -> Unit
) {
    val formState by viewModel.customerFormState.collectAsState()
    var showOcrScanner by remember { mutableStateOf(false) }

    if (showOcrScanner) {
        DocumentScanDialog(
            targetType = "Customer",
            onDismiss = { showOcrScanner = false },
            onDataExtracted = { scanned ->
                viewModel.updateCustomerForm { s ->
                    s.copy(
                        name = if (scanned.name.isNotBlank()) scanned.name else s.name,
                        mobile = if (scanned.mobileNumber.isNotBlank()) scanned.mobileNumber else s.mobile,
                        gstin = if (scanned.gstin.isNotBlank()) scanned.gstin else s.gstin
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
                Text("Add Customer", fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = { showOcrScanner = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan Card", fontSize = 11.sp)
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
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { viewModel.updateCustomerForm { s -> s.copy(name = it) } },
                    label = { Text("Customer / Firm Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.mobile,
                    onValueChange = { viewModel.updateCustomerForm { s -> s.copy(mobile = it) } },
                    label = { Text("Contact Mobile (10 Digits) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = formState.mobileError != null,
                    supportingText = formState.mobileError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.address,
                    onValueChange = { viewModel.updateCustomerForm { s -> s.copy(address = it) } },
                    label = { Text("Billing Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.gstin,
                    onValueChange = { viewModel.updateCustomerForm { s -> s.copy(gstin = it) } },
                    label = { Text("GSTIN (Optional 15 chars)") },
                    isError = formState.gstinError != null,
                    supportingText = formState.gstinError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.submitCustomer {
                        onDismiss()
                    }
                },
                enabled = formState.isValid
            ) {
                Text("Save Customer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
