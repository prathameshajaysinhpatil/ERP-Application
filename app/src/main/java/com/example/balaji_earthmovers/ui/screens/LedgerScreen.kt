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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.balaji_earthmovers.data.local.entity.ExpenseEntity
import com.example.balaji_earthmovers.data.local.entity.ExpenseType
import com.example.balaji_earthmovers.data.local.entity.IncomeEntity
import com.example.balaji_earthmovers.data.local.entity.PaymentMode
import com.example.balaji_earthmovers.ui.theme.ExpenseRed
import com.example.balaji_earthmovers.ui.theme.ProfitGreen
import com.example.balaji_earthmovers.ui.util.LocalStrings
import com.example.balaji_earthmovers.ui.viewmodel.BalajiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    viewModel: BalajiViewModel,
    onBack: (() -> Unit)? = null
) {
    val strings = LocalStrings.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    var expenseToEditPending by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }
    var incomeToEditPending by remember { mutableStateOf<IncomeEntity?>(null) }
    var incomeToEdit by remember { mutableStateOf<IncomeEntity?>(null) }
    var incomeToDelete by remember { mutableStateOf<IncomeEntity?>(null) }

    val expenses by viewModel.allExpenses.collectAsState()
    val incomes by viewModel.allIncomes.collectAsState()
    val totalExpense by viewModel.totalExpenses.collectAsState()
    val totalIncome by viewModel.totalCollectedIncome.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${strings.tabExpense} & ${strings.tabIncome}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showExpenseDialog = true else showPaymentDialog = true
                },
                containerColor = if (selectedTab == 0) ExpenseRed else ProfitGreen,
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = if (selectedTab == 0) strings.addExpense else strings.addIncome
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
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                            Text("${strings.tabExpense} (₹${"%.0f".format(totalExpense)})", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(16.dp))
                            Text("${strings.tabIncome} (₹${"%.0f".format(totalIncome)})", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // Expense List
                if (expenses.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No expenses logged yet. Tap '+' to add expense.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(expenses, key = { it.id }) { expense ->
                            ExpenseItemCard(
                                expense = expense,
                                onEdit = { expenseToEditPending = expense },
                                onDelete = { expenseToDelete = expense }
                            )
                        }
                    }
                }
            } else {
                // Income List
                if (incomes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No income records found. Tap '+' to add income.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(incomes, key = { it.id }) { income ->
                            IncomeItemCard(
                                income = income,
                                onEdit = { incomeToEditPending = income },
                                onDelete = { incomeToDelete = income }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExpenseDialog) {
        AddExpenseDialog(
            viewModel = viewModel,
            onDismiss = { showExpenseDialog = false }
        )
    }

    expenseToEditPending?.let { expense ->
        AdminPasswordEditDialog(
            itemType = "Expense",
            itemName = "${expense.expenseName} (₹${"%.2f".format(expense.amount)})",
            viewModel = viewModel,
            onDismiss = { expenseToEditPending = null },
            onVerified = {
                expenseToEdit = expense
                expenseToEditPending = null
            }
        )
    }

    expenseToEdit?.let { expense ->
        EditExpenseDialog(
            expense = expense,
            viewModel = viewModel,
            onDismiss = { expenseToEdit = null }
        )
    }

    expenseToDelete?.let { expense ->
        AdminPasswordDeleteDialog(
            itemType = "Expense",
            itemName = "${expense.expenseName} (₹${"%.2f".format(expense.amount)})",
            viewModel = viewModel,
            onDismiss = { expenseToDelete = null },
            onConfirmDelete = {
                viewModel.deleteExpense(expense.id)
            }
        )
    }

    if (showPaymentDialog) {
        AddIncomePaymentDialog(
            viewModel = viewModel,
            onDismiss = { showPaymentDialog = false }
        )
    }

    incomeToEditPending?.let { income ->
        AdminPasswordEditDialog(
            itemType = "Income",
            itemName = "Payment (${income.paymentMode.name}) - ₹${"%.2f".format(income.paidAmount)}",
            viewModel = viewModel,
            onDismiss = { incomeToEditPending = null },
            onVerified = {
                incomeToEdit = income
                incomeToEditPending = null
            }
        )
    }

    incomeToEdit?.let { income ->
        EditIncomeDialog(
            income = income,
            viewModel = viewModel,
            onDismiss = { incomeToEdit = null }
        )
    }

    incomeToDelete?.let { income ->
        AdminPasswordDeleteDialog(
            itemType = "Income",
            itemName = "Payment (${income.paymentMode.name}) - ₹${"%.2f".format(income.paidAmount)}",
            viewModel = viewModel,
            onDismiss = { incomeToDelete = null },
            onConfirmDelete = {
                viewModel.deleteIncome(income.id)
            }
        )
    }
}

@Composable
fun ExpenseItemCard(
    expense: ExpenseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(expense.date) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expense.date))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.expenseName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Type: ${expense.expenseType.name} • $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (expense.description.isNotBlank()) {
                    Text(
                        expense.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "- ₹${"%.2f".format(expense.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = ExpenseRed
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Expense", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Expense", tint = ExpenseRed)
                }
            }
        }
    }
}

@Composable
fun IncomeItemCard(
    income: IncomeEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(income.date) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(income.date))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Payment Received (${income.paymentMode.name})",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Date: $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (income.transactionReferenceId.isNotBlank()) {
                    Text(
                        "Ref: ${income.transactionReferenceId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (income.pendingAmount > 0) {
                    Text(
                        "Balance Pending: ₹${"%.2f".format(income.pendingAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ExpenseRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "+ ₹${"%.2f".format(income.paidAmount)}",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = ProfitGreen
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Income", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Income", tint = ExpenseRed)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseDialog(
    expense: ExpenseEntity,
    viewModel: BalajiViewModel,
    onDismiss: () -> Unit
) {
    var expenseName by remember { mutableStateOf(expense.expenseName) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var expenseType by remember { mutableStateOf(expense.expenseType) }
    var description by remember { mutableStateOf(expense.description) }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = expenseName,
                    onValueChange = { expenseName = it },
                    label = { Text("Expense Title *") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = expenseType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expense Type *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        ExpenseType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    expenseType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: expense.amount
                    val updated = expense.copy(
                        expenseName = expenseName.trim(),
                        expenseType = expenseType,
                        amount = amt,
                        description = description.trim()
                    )
                    viewModel.editExpense(updated) {
                        onDismiss()
                    }
                },
                enabled = expenseName.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0.0
            ) {
                Text("Update Expense")
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
fun EditIncomeDialog(
    income: IncomeEntity,
    viewModel: BalajiViewModel,
    onDismiss: () -> Unit
) {
    var paidAmount by remember { mutableStateOf(income.paidAmount.toString()) }
    var pendingAmount by remember { mutableStateOf(income.pendingAmount.toString()) }
    var paymentMode by remember { mutableStateOf(income.paymentMode) }
    var transactionRef by remember { mutableStateOf(income.transactionReferenceId) }
    var modeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Income", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = paidAmount,
                    onValueChange = { paidAmount = it },
                    label = { Text("Paid Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pendingAmount,
                    onValueChange = { pendingAmount = it },
                    label = { Text("Pending Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = modeExpanded,
                    onExpandedChange = { modeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = paymentMode.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Mode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = modeExpanded,
                        onDismissRequest = { modeExpanded = false }
                    ) {
                        PaymentMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.name) },
                                onClick = {
                                    paymentMode = mode
                                    modeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = transactionRef,
                    onValueChange = { transactionRef = it },
                    label = { Text("Reference / UTR / Cheque Number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val paid = paidAmount.toDoubleOrNull() ?: income.paidAmount
                    val pending = pendingAmount.toDoubleOrNull() ?: income.pendingAmount
                    val updated = income.copy(
                        paidAmount = paid,
                        pendingAmount = pending,
                        paymentMode = paymentMode,
                        transactionReferenceId = transactionRef.trim()
                    )
                    viewModel.editIncome(updated) {
                        onDismiss()
                    }
                },
                enabled = (paidAmount.toDoubleOrNull() ?: -1.0) >= 0.0
            ) {
                Text("Update Income")
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
fun AddExpenseDialog(
    viewModel: BalajiViewModel,
    onDismiss: () -> Unit
) {
    val formState by viewModel.expenseFormState.collectAsState()
    val drivers by viewModel.allDrivers.collectAsState()
    val vehicles by viewModel.allVehicles.collectAsState()

    var typeExpanded by remember { mutableStateOf(false) }
    var driverExpanded by remember { mutableStateOf(false) }
    var vehicleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Expense Name
                OutlinedTextField(
                    value = formState.expenseName,
                    onValueChange = { viewModel.updateExpenseForm { s -> s.copy(expenseName = it) } },
                    label = { Text("Expense Title (e.g. Diesel Refill) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Expense Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = formState.expenseType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expense Type *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        ExpenseType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    viewModel.updateExpenseForm { s -> s.copy(expenseType = type) }
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = formState.amount,
                    onValueChange = { viewModel.updateExpenseForm { s -> s.copy(amount = it) } },
                    label = { Text("Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = formState.amountError != null,
                    supportingText = formState.amountError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Conditional Driver Dropdown for SALARY
                if (formState.expenseType == ExpenseType.SALARY) {
                    ExposedDropdownMenuBox(
                        expanded = driverExpanded,
                        onExpandedChange = { driverExpanded = it }
                    ) {
                        val selectedDriver = drivers.find { it.id == formState.linkedDriverId }
                        OutlinedTextField(
                            value = selectedDriver?.driverName ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Linked Driver (Mandatory for SALARY) *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = driverExpanded) },
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
                                        viewModel.updateExpenseForm { s -> s.copy(linkedDriverId = driver.id, driverError = null) }
                                        driverExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Conditional Vehicle Dropdown for FUEL / MAINTENANCE
                if (formState.expenseType == ExpenseType.FUEL || formState.expenseType == ExpenseType.MAINTENANCE) {
                    ExposedDropdownMenuBox(
                        expanded = vehicleExpanded,
                        onExpandedChange = { vehicleExpanded = it }
                    ) {
                        val selectedVehicle = vehicles.find { it.id == formState.linkedVehicleId }
                        OutlinedTextField(
                            value = selectedVehicle?.let { "${it.vehicleName} (${it.vehicleNumber})" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Linked Vehicle (Mandatory for ${formState.expenseType.name}) *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleExpanded) },
                            isError = formState.vehicleError != null,
                            supportingText = formState.vehicleError?.let { { Text(it) } },
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
                                    text = { Text("${vehicle.vehicleName} (${vehicle.vehicleNumber})") },
                                    onClick = {
                                        viewModel.updateExpenseForm { s -> s.copy(linkedVehicleId = vehicle.id, vehicleError = null) }
                                        vehicleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Description (Mandatory when type is OTHER)
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = { viewModel.updateExpenseForm { s -> s.copy(description = it) } },
                    label = { Text(if (formState.expenseType == ExpenseType.OTHER) "Description (Mandatory for OTHER) *" else "Description / Notes") },
                    isError = formState.descriptionError != null,
                    supportingText = formState.descriptionError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.submitExpense {
                        onDismiss()
                    }
                },
                enabled = formState.isValid
            ) {
                Text("Save Expense")
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
fun AddIncomePaymentDialog(
    viewModel: BalajiViewModel,
    onDismiss: () -> Unit
) {
    val formState by viewModel.paymentFormState.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()

    var customerExpanded by remember { mutableStateOf(false) }
    var modeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Income", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = customerExpanded,
                    onExpandedChange = { customerExpanded = it }
                ) {
                    val selected = customers.find { it.id == formState.customerId }
                    OutlinedTextField(
                        value = selected?.let { "${it.name} (Pending: ₹${"%.0f".format(it.pendingBalance)})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Customer *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = customerExpanded,
                        onDismissRequest = { customerExpanded = false }
                    ) {
                        customers.forEach { cust ->
                            DropdownMenuItem(
                                text = { Text("${cust.name} - Pending ₹${"%.0f".format(cust.pendingBalance)}") },
                                onClick = {
                                    viewModel.updatePaymentForm { s -> s.copy(customerId = cust.id) }
                                    customerExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = formState.amount,
                    onValueChange = { viewModel.updatePaymentForm { s -> s.copy(amount = it) } },
                    label = { Text("Amount Received (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = formState.amountError != null,
                    supportingText = formState.amountError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = modeExpanded,
                    onExpandedChange = { modeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = formState.paymentMode.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Mode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = modeExpanded,
                        onDismissRequest = { modeExpanded = false }
                    ) {
                        PaymentMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.name) },
                                onClick = {
                                    viewModel.updatePaymentForm { s -> s.copy(paymentMode = mode) }
                                    modeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = formState.transactionRef,
                    onValueChange = { viewModel.updatePaymentForm { s -> s.copy(transactionRef = it) } },
                    label = { Text("Reference / UTR / Cheque Number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.submitCustomerPayment {
                        onDismiss()
                    }
                },
                enabled = formState.isValid
            ) {
                Text("Save Income")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
