package com.example.balaji_earthmovers.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.balaji_earthmovers.data.local.entity.CustomerEntity
import com.example.balaji_earthmovers.data.local.entity.PaymentMode
import com.example.balaji_earthmovers.ui.theme.ExpenseRed
import com.example.balaji_earthmovers.ui.theme.ProfitGreen
import com.example.balaji_earthmovers.ui.util.CustomerShareUtils
import com.example.balaji_earthmovers.ui.viewmodel.BalajiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAgingScreen(
    viewModel: BalajiViewModel,
    onBack: () -> Unit,
    onOpenCustomerPassbook: (CustomerEntity) -> Unit = {}
) {
    val customers by viewModel.allCustomers.collectAsState()
    val totalPending by viewModel.totalPendingReceivables.collectAsState()

    var paymentCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var selectedPassbookCustomer by remember { mutableStateOf<CustomerEntity?>(null) }

    if (selectedPassbookCustomer != null) {
        CustomerPassbookScreen(
            customer = selectedPassbookCustomer!!,
            viewModel = viewModel,
            onBack = { selectedPassbookCustomer = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Customer Receivables", fontWeight = FontWeight.Bold)
                        Text(
                            "Total amount, received, pending & ledger",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Simplified Summary Card: Total Billed, Total Received, Total Pending
            val totalBilled = customers.sumOf { it.totalBilled }
            val totalReceived = customers.sumOf { it.totalPaid }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total Remaining Receivable",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "₹${"%.2f".format(totalPending)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (totalPending > 0) ExpenseRed else ProfitGreen
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Billed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${"%.2f".format(totalBilled)}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Received", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${"%.2f".format(totalReceived)}", fontWeight = FontWeight.SemiBold, color = ProfitGreen, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Customer List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "All Customers (${customers.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No customer records found.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp, top = 4.dp)
                ) {
                    items(customers, key = { it.id }) { customer ->
                        SimpleCustomerReceivableCard(
                            customer = customer,
                            onOpenPassbook = { selectedPassbookCustomer = customer },
                            onReceivePayment = { paymentCustomer = customer }
                        )
                    }
                }
            }
        }
    }

    paymentCustomer?.let { customer ->
        RecordCustomerPaymentDialog(
            customer = customer,
            viewModel = viewModel,
            onDismiss = { paymentCustomer = null }
        )
    }
}

@Composable
fun SimpleCustomerReceivableCard(
    customer: CustomerEntity,
    onOpenPassbook: () -> Unit,
    onReceivePayment: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            customer.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 15.sp
                        )
                        if (customer.mobileNumber.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(customer.mobileNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
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
                        Text("Pending", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        Text(
                            "₹${"%.2f".format(customer.pendingBalance)}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Billed: ₹${"%.0f".format(customer.totalBilled)} • Paid: ₹${"%.0f".format(customer.totalPaid)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (customer.mobileNumber.isNotBlank()) {
                        var showContactMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { showContactMenu = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Contact Customer", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            DropdownMenu(
                                expanded = showContactMenu,
                                onDismissRequest = { showContactMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Call (${customer.mobileNumber})") },
                                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = ProfitGreen) },
                                    onClick = {
                                        showContactMenu = false
                                        CustomerShareUtils.makeCall(context, customer.mobileNumber)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("WhatsApp Bill") },
                                    leadingIcon = { Icon(Icons.Default.ChatBubble, contentDescription = null, tint = ProfitGreen) },
                                    onClick = {
                                        showContactMenu = false
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
                                    text = { Text("SMS Bill") },
                                    leadingIcon = { Icon(Icons.Default.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    onClick = {
                                        showContactMenu = false
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

                    OutlinedButton(
                        onClick = onOpenPassbook,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Passbook", style = MaterialTheme.typography.labelSmall)
                    }

                    if (customer.pendingBalance > 0) {
                        Button(
                            onClick = onReceivePayment,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Receive", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordCustomerPaymentDialog(
    customer: CustomerEntity,
    viewModel: BalajiViewModel,
    onDismiss: () -> Unit
) {
    val formState by viewModel.paymentFormState.collectAsState()
    var modeExpanded by remember { mutableStateOf(false) }

    // Amount is initialized as empty "" so user enters the amount explicitly
    remember {
        viewModel.updatePaymentForm { s -> s.copy(customerId = customer.id, amount = "") }
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
            Text("Receive Payment: ${customer.name}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Pending Balance: ₹${"%.2f".format(customer.pendingBalance)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ExpenseRed
                )

                OutlinedTextField(
                    value = formState.amount,
                    onValueChange = { viewModel.updatePaymentForm { s -> s.copy(amount = it) } },
                    label = { Text("Payment Amount (₹) *") },
                    placeholder = { Text("Enter payment amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = formState.amountError != null,
                    supportingText = formState.amountError?.let { { Text(it) } },
                    shape = RoundedCornerShape(10.dp),
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
                        shape = RoundedCornerShape(10.dp),
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
                    label = { Text("Reference / UTR / Cheque No.") },
                    shape = RoundedCornerShape(10.dp),
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
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
