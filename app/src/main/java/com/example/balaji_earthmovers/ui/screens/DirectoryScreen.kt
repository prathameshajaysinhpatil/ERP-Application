package com.example.balaji_earthmovers.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.balaji_earthmovers.data.local.entity.CustomerEntity
import com.example.balaji_earthmovers.data.local.entity.DriverEntity
import com.example.balaji_earthmovers.data.security.CryptoManager
import com.example.balaji_earthmovers.ui.theme.ExpenseRed
import com.example.balaji_earthmovers.ui.theme.ProfitGreen
import com.example.balaji_earthmovers.ui.util.CustomerShareUtils
import com.example.balaji_earthmovers.ui.util.EntityAvatar
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
    var driverForDetail by remember { mutableStateOf<DriverEntity?>(null) }

    var customerToEditPending by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerForDetail by remember { mutableStateOf<CustomerEntity?>(null) }

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
                                onOpenDetail = { driverForDetail = driver }
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
                                onOpenDetail = { customerForDetail = customer }
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

    driverForDetail?.let { driver ->
        DriverDetailDialog(
            driver = driver,
            onOpenPassbook = {
                onSelectDriverPassbook(driver)
                driverForDetail = null
            },
            onEdit = {
                driverToEditPending = driver
                driverForDetail = null
            },
            onDelete = {
                driverToDelete = driver
                driverForDetail = null
            },
            onDismiss = { driverForDetail = null }
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

    customerForDetail?.let { customer ->
        CustomerDetailDialog(
            customer = customer,
            onOpenPassbook = {
                onSelectCustomerPassbook(customer)
                customerForDetail = null
            },
            onEdit = {
                customerToEditPending = customer
                customerForDetail = null
            },
            onDelete = {
                customerToDelete = customer
                customerForDetail = null
            },
            onDismiss = { customerForDetail = null }
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
    onOpenDetail: () -> Unit
) {
    val maskedAadhaar = remember(driver.adharNumber) {
        val decrypted = CryptoManager.decrypt(driver.adharNumber)
        CryptoManager.maskAadhaar(decrypted)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail() },
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
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EntityAvatar(
                        imageUri = driver.imageUri,
                        fallbackIcon = Icons.Default.Person,
                        contentDescription = driver.driverName,
                        size = 50.dp
                    )
                    Column {
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
fun DriverDetailDialog(
    driver: DriverEntity,
    onOpenPassbook: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val decryptedAadhaar = remember(driver.adharNumber) {
        CryptoManager.decrypt(driver.adharNumber)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EntityAvatar(
                    imageUri = driver.imageUri,
                    fallbackIcon = Icons.Default.Person,
                    contentDescription = driver.driverName,
                    size = 52.dp
                )
                Column {
                    Text(driver.driverName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Driver / चालक", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Mobile Number:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(driver.mobileNumber, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                if (driver.mobileNumber.isNotBlank()) {
                                    IconButton(
                                        onClick = { CustomerShareUtils.makeCall(context, driver.mobileNumber) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                        if (driver.address.isNotBlank()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Address:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(driver.address, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Aadhaar Number:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(decryptedAadhaar.ifBlank { "Not provided" }, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Monthly Salary:", style = MaterialTheme.typography.bodySmall)
                            Text("₹${"%.0f".format(driver.monthlySalary)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Paid Salary:", style = MaterialTheme.typography.bodySmall)
                            Text("₹${"%.0f".format(driver.givenSalary)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = ProfitGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pending Salary:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("₹${"%.0f".format(driver.pendingSalary)}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyMedium, color = if (driver.pendingSalary > 0) ExpenseRed else ProfitGreen)
                        }
                    }
                }

                OutlinedButton(
                    onClick = onOpenPassbook,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Driver Passbook / पासबुक उघडा", style = MaterialTheme.typography.labelMedium)
                }

                HorizontalDivider()

                // Internal Actions: Edit & Delete
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
fun CustomerDirectoryCard(
    customer: CustomerEntity,
    onOpenPassbook: () -> Unit,
    onOpenDetail: () -> Unit
) {
    val context = LocalContext.current
    var contactMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail() },
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
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EntityAvatar(
                        imageUri = customer.imageUri,
                        fallbackIcon = Icons.Default.Business,
                        contentDescription = customer.name,
                        size = 50.dp
                    )
                    Column {
                        Text(customer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(customer.mobileNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (customer.mobileNumber.isNotBlank()) {
                                Box {
                                    FilledTonalButton(
                                        onClick = { contactMenuExpanded = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Contact", modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Contact", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    DropdownMenu(
                                        expanded = contactMenuExpanded,
                                        onDismissRequest = { contactMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Call / थेट कॉल") },
                                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            onClick = {
                                                contactMenuExpanded = false
                                                CustomerShareUtils.makeCall(context, customer.mobileNumber)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("WhatsApp Bill / व्हॉट्सअॅप बिल") },
                                            leadingIcon = { Icon(Icons.Default.ChatBubble, contentDescription = null, tint = ProfitGreen) },
                                            onClick = {
                                                contactMenuExpanded = false
                                                CustomerShareUtils.openWhatsApp(
                                                    context = context,
                                                    mobileNumber = customer.mobileNumber,
                                                    customerName = customer.name,
                                                    totalAmount = customer.totalBilled,
                                                    pendingAmount = customer.pendingBalance
                                                )
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("SMS Bill / एसएमएस बिल") },
                                            leadingIcon = { Icon(Icons.Default.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            onClick = {
                                                contactMenuExpanded = false
                                                CustomerShareUtils.openSms(
                                                    context = context,
                                                    mobileNumber = customer.mobileNumber,
                                                    customerName = customer.name,
                                                    totalAmount = customer.totalBilled,
                                                    pendingAmount = customer.pendingBalance
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        if (customer.gstin.isNotBlank()) {
                            Text("GSTIN: ${customer.gstin}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
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

@Composable
fun CustomerDetailDialog(
    customer: CustomerEntity,
    onOpenPassbook: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EntityAvatar(
                    imageUri = customer.imageUri,
                    fallbackIcon = Icons.Default.Business,
                    contentDescription = customer.name,
                    size = 52.dp
                )
                Column {
                    Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Customer / ग्राहक", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Mobile Number:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(customer.mobileNumber, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                if (customer.mobileNumber.isNotBlank()) {
                                    IconButton(
                                        onClick = { CustomerShareUtils.makeCall(context, customer.mobileNumber) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
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
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ChatBubble, contentDescription = "WhatsApp", tint = ProfitGreen, modifier = Modifier.size(16.dp))
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
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Sms, contentDescription = "SMS", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                        if (customer.address.isNotBlank()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Address:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(customer.address, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (customer.gstin.isNotBlank()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("GSTIN:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(customer.gstin, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Billed:", style = MaterialTheme.typography.bodySmall)
                            Text("₹${"%.2f".format(customer.totalBilled)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid:", style = MaterialTheme.typography.bodySmall)
                            Text("₹${"%.2f".format(customer.totalPaid)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = ProfitGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Receivable Balance:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("₹${"%.2f".format(customer.pendingBalance)}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyMedium, color = if (customer.pendingBalance > 0) ExpenseRed else ProfitGreen)
                        }
                    }
                }

                OutlinedButton(
                    onClick = onOpenPassbook,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Customer Khata / खातेवही उघडा", style = MaterialTheme.typography.labelMedium)
                }

                HorizontalDivider()

                // Internal Actions: Edit & Delete
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
fun EditDriverDialog(
    driver: DriverEntity,
    viewModel: BalajiViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(driver.driverName) }
    var mobile by remember { mutableStateOf(driver.mobileNumber) }
    var address by remember { mutableStateOf(driver.address) }
    val initialAadhaar = remember(driver.adharNumber) {
        CryptoManager.decrypt(driver.adharNumber)
    }
    var aadhaar by remember { mutableStateOf(initialAadhaar) }
    var monthlySalary by remember { mutableStateOf(driver.monthlySalary.toString()) }
    var imageUri by remember { mutableStateOf(driver.imageUri) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            imageUri = it.toString()
        }
    }

    var speechTarget by remember { mutableStateOf<String?>(null) }
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim() ?: ""
            if (spoken.isNotBlank()) {
                when (speechTarget) {
                    "name" -> name = spoken
                    "mobile" -> {
                        val digits = spoken.filter { it.isDigit() }
                        mobile = if (digits.length >= 10) digits.takeLast(10) else digits
                    }
                    "address" -> address = spoken
                    "salary" -> {
                        val digits = spoken.filter { it.isDigit() || it == '.' }
                        if (digits.isNotBlank()) monthlySalary = digits
                    }
                }
            }
        }
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
        title = { Text("Edit Driver / चालक बदला", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Photo row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EntityAvatar(imageUri = imageUri, fallbackIcon = Icons.Default.Person, contentDescription = "Driver Photo", size = 56.dp)
                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (imageUri != null) "Change Photo" else "Upload Photo (चालक फोटो)", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Driver Full Name *") },
                    trailingIcon = {
                        IconButton(onClick = {
                            speechTarget = "name"
                            try {
                                speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "नाव सांगा (Speak Driver Name)")
                                })
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number (10 Digits) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    trailingIcon = {
                        IconButton(onClick = {
                            speechTarget = "mobile"
                            try {
                                speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "मोबाईल नंबर सांगा (Speak Mobile Number)")
                                })
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Residential Address") },
                    trailingIcon = {
                        IconButton(onClick = {
                            speechTarget = "address"
                            try {
                                speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "पत्ता सांगा (Speak Address)")
                                })
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
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
                    trailingIcon = {
                        IconButton(onClick = {
                            speechTarget = "salary"
                            try {
                                speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "पगार सांगा (Speak Monthly Salary)")
                                })
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
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
                        monthlySalary = salary,
                        imageUri = imageUri
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
    val context = LocalContext.current
    var name by remember { mutableStateOf(customer.name) }
    var mobile by remember { mutableStateOf(customer.mobileNumber) }
    var address by remember { mutableStateOf(customer.address) }
    var gstin by remember { mutableStateOf(customer.gstin) }
    var imageUri by remember { mutableStateOf(customer.imageUri) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            imageUri = it.toString()
        }
    }

    var speechTarget by remember { mutableStateOf<String?>(null) }
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim() ?: ""
            if (spoken.isNotBlank()) {
                when (speechTarget) {
                    "name" -> name = spoken
                    "mobile" -> {
                        val digits = spoken.filter { it.isDigit() }
                        mobile = if (digits.length >= 10) digits.takeLast(10) else digits
                    }
                    "address" -> address = spoken
                    "gstin" -> gstin = spoken.replace(" ", "").uppercase()
                }
            }
        }
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
        title = { Text("Edit Customer / ग्राहक बदला", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Photo row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EntityAvatar(imageUri = imageUri, fallbackIcon = Icons.Default.Business, contentDescription = "Customer Photo", size = 56.dp)
                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (imageUri != null) "Change Photo" else "Upload Photo (ग्राहक फोटो)", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer / Firm Name *") },
                    trailingIcon = {
                        IconButton(onClick = {
                            speechTarget = "name"
                            try {
                                speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "ग्राहकाचे नाव सांगा (Speak Customer Name)")
                                })
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Contact Mobile (10 Digits) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    trailingIcon = {
                        IconButton(onClick = {
                            speechTarget = "mobile"
                            try {
                                speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "मोबाईल नंबर सांगा (Speak Mobile Number)")
                                })
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Billing Address") },
                    trailingIcon = {
                        IconButton(onClick = {
                            speechTarget = "address"
                            try {
                                speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "पत्ता सांगा (Speak Address)")
                                })
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
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
                        gstin = gstin.trim().uppercase(),
                        imageUri = imageUri
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
    val context = LocalContext.current
    val formState by viewModel.driverFormState.collectAsState()
    var showOcrScanner by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            viewModel.updateDriverForm { s -> s.copy(imageUri = it.toString()) }
        }
    }

    var currentSpeechTarget by remember { mutableStateOf<String?>(null) }
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim() ?: ""
            if (spoken.isNotBlank()) {
                when (currentSpeechTarget) {
                    "name" -> viewModel.updateDriverForm { s -> s.copy(name = spoken) }
                    "mobile" -> {
                        val digits = spoken.filter { it.isDigit() }
                        viewModel.updateDriverForm { s -> s.copy(mobile = if (digits.length >= 10) digits.takeLast(10) else digits) }
                    }
                    "address" -> viewModel.updateDriverForm { s -> s.copy(address = spoken) }
                    "aadhaar" -> {
                        val digits = spoken.filter { it.isDigit() }
                        viewModel.updateDriverForm { s -> s.copy(aadhaar = digits) }
                    }
                    "salary" -> {
                        val digits = spoken.filter { it.isDigit() || it == '.' }
                        viewModel.updateDriverForm { s -> s.copy(monthlySalary = digits) }
                    }
                    "auto_name" -> {
                        viewModel.updateDriverForm { s -> s.copy(name = spoken) }
                        currentSpeechTarget = "auto_mobile"
                    }
                    "auto_mobile" -> {
                        val digits = spoken.filter { it.isDigit() }
                        viewModel.updateDriverForm { s -> s.copy(mobile = if (digits.length >= 10) digits.takeLast(10) else digits) }
                        currentSpeechTarget = "auto_salary"
                    }
                    "auto_salary" -> {
                        val digits = spoken.filter { it.isDigit() || it == '.' }
                        viewModel.updateDriverForm { s -> s.copy(monthlySalary = digits) }
                        currentSpeechTarget = null
                    }
                }
            }
        }
    }

    LaunchedEffect(currentSpeechTarget) {
        when (currentSpeechTarget) {
            "auto_name" -> {
                try {
                    speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "पायरी १: चालकाचे नाव सांगा (Step 1: Speak Driver Name)")
                    })
                } catch (_: Exception) { currentSpeechTarget = null }
            }
            "auto_mobile" -> {
                try {
                    speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "पायरी २: मोबाईल नंबर सांगा (Step 2: Speak Mobile Number)")
                    })
                } catch (_: Exception) { currentSpeechTarget = null }
            }
            "auto_salary" -> {
                try {
                    speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "पायरी ३: मासिक पगार सांगा (Step 3: Speak Monthly Salary)")
                    })
                } catch (_: Exception) { currentSpeechTarget = null }
            }
        }
    }

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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { currentSpeechTarget = "auto_name" },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Voice Fill", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { showOcrScanner = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan", fontSize = 11.sp)
                    }
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
                // Photo selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EntityAvatar(imageUri = formState.imageUri, fallbackIcon = Icons.Default.Person, contentDescription = "Driver Photo", size = 52.dp)
                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (formState.imageUri != null) "Change Photo" else "Add Photo (चालक फोटो)", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { viewModel.updateDriverForm { s -> s.copy(name = it) } },
                    label = { Text("Driver Full Name *") },
                    trailingIcon = {
                        IconButton(onClick = {
                            currentSpeechTarget = "name"
                            try {
                                speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "नाव सांगा (Speak Driver Name)")
                                })
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.mobile,
                    onValueChange = { viewModel.updateDriverForm { s -> s.copy(mobile = it) } },
                    label = { Text("Mobile Number (10 Digits) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = formState.mobileError != null,
                    supportingText = formState.mobileError?.let { { Text(it) } },
                    trailingIcon = {
                        IconButton(onClick = {
                            currentSpeechTarget = "mobile"
                            try {
                                speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "मोबाईल नंबर सांगा (Speak Mobile Number)")
                                })
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.address,
                    onValueChange = { viewModel.updateDriverForm { s -> s.copy(address = it) } },
                    label = { Text("Residential Address") },
                    trailingIcon = {
                        IconButton(onClick = {
                            currentSpeechTarget = "address"
                            try {
                                speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "पत्ता सांगा (Speak Address)")
                                })
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
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
                    trailingIcon = {
                        IconButton(onClick = {
                            currentSpeechTarget = "salary"
                            try {
                                speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "मासिक पगार सांगा (Speak Salary)")
                                })
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
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
    val context = LocalContext.current
    val formState by viewModel.customerFormState.collectAsState()
    var showOcrScanner by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            viewModel.updateCustomerForm { s -> s.copy(imageUri = it.toString()) }
        }
    }

    var currentSpeechTarget by remember { mutableStateOf<String?>(null) }
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim() ?: ""
            if (spoken.isNotBlank()) {
                when (currentSpeechTarget) {
                    "name" -> viewModel.updateCustomerForm { s -> s.copy(name = spoken) }
                    "mobile" -> {
                        val digits = spoken.filter { it.isDigit() }
                        viewModel.updateCustomerForm { s -> s.copy(mobile = if (digits.length >= 10) digits.takeLast(10) else digits) }
                    }
                    "address" -> viewModel.updateCustomerForm { s -> s.copy(address = spoken) }
                    "gstin" -> viewModel.updateCustomerForm { s -> s.copy(gstin = spoken.replace(" ", "").uppercase()) }
                    "auto_name" -> {
                        viewModel.updateCustomerForm { s -> s.copy(name = spoken) }
                        currentSpeechTarget = "auto_mobile"
                    }
                    "auto_mobile" -> {
                        val digits = spoken.filter { it.isDigit() }
                        viewModel.updateCustomerForm { s -> s.copy(mobile = if (digits.length >= 10) digits.takeLast(10) else digits) }
                        currentSpeechTarget = "auto_address"
                    }
                    "auto_address" -> {
                        viewModel.updateCustomerForm { s -> s.copy(address = spoken) }
                        currentSpeechTarget = null
                    }
                }
            }
        }
    }

    LaunchedEffect(currentSpeechTarget) {
        when (currentSpeechTarget) {
            "auto_name" -> {
                try {
                    speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "पायरी १: ग्राहकाचे नाव सांगा (Step 1: Speak Customer Name)")
                    })
                } catch (_: Exception) { currentSpeechTarget = null }
            }
            "auto_mobile" -> {
                try {
                    speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "पायरी २: मोबाईल नंबर सांगा (Step 2: Speak Mobile Number)")
                    })
                } catch (_: Exception) { currentSpeechTarget = null }
            }
            "auto_address" -> {
                try {
                    speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "पायरी ३: पत्ता सांगा (Step 3: Speak Billing Address)")
                    })
                } catch (_: Exception) {}
            }
        }
    }

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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { currentSpeechTarget = "auto_name" },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Voice Fill", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { showOcrScanner = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan Card", fontSize = 11.sp)
                    }
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
                // Photo selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EntityAvatar(imageUri = formState.imageUri, fallbackIcon = Icons.Default.Business, contentDescription = "Customer Photo", size = 52.dp)
                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (formState.imageUri != null) "Change Photo" else "Add Photo (ग्राहक फोटो)", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { viewModel.updateCustomerForm { s -> s.copy(name = it) } },
                    label = { Text("Customer / Firm Name *") },
                    trailingIcon = {
                        IconButton(onClick = {
                            currentSpeechTarget = "name"
                            try {
                                speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "ग्राहकाचे नाव सांगा (Speak Customer Name)")
                                })
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.mobile,
                    onValueChange = { viewModel.updateCustomerForm { s -> s.copy(mobile = it) } },
                    label = { Text("Contact Mobile (10 Digits) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = formState.mobileError != null,
                    supportingText = formState.mobileError?.let { { Text(it) } },
                    trailingIcon = {
                        IconButton(onClick = {
                            currentSpeechTarget = "mobile"
                            try {
                                speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "मोबाईल नंबर सांगा (Speak Mobile Number)")
                                })
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.address,
                    onValueChange = { viewModel.updateCustomerForm { s -> s.copy(address = it) } },
                    label = { Text("Billing Address") },
                    trailingIcon = {
                        IconButton(onClick = {
                            currentSpeechTarget = "address"
                            try {
                                speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "पत्ता सांगा (Speak Billing Address)")
                                })
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
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
