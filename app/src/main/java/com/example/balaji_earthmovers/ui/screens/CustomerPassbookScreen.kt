package com.example.balaji_earthmovers.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balaji_earthmovers.data.local.entity.CustomerEntity
import com.example.balaji_earthmovers.data.local.entity.IncomeEntity
import com.example.balaji_earthmovers.data.local.entity.WorkEntity
import com.example.balaji_earthmovers.ui.theme.ExpenseRed
import com.example.balaji_earthmovers.ui.theme.ProfitGreen
import com.example.balaji_earthmovers.ui.util.CustomerShareUtils
import com.example.balaji_earthmovers.ui.viewmodel.BalajiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class CustomerPassbookEntry(val timestamp: Long) {
    data class WorkEntry(val work: WorkEntity) : CustomerPassbookEntry(work.date)
    data class PaymentEntry(val income: IncomeEntity) : CustomerPassbookEntry(income.date)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerPassbookScreen(
    customer: CustomerEntity,
    viewModel: BalajiViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val customerHistory by viewModel.getCustomerWithHistory(customer.id)
        .collectAsState(initial = null)

    var showPaymentDialog by remember { mutableStateOf(false) }

    val liveCustomer = customerHistory?.customer ?: customer
    val works = customerHistory?.works ?: emptyList()
    val incomes = customerHistory?.incomes ?: emptyList()

    // Combine and sort chronologically (most recent first)
    val entries = remember(works, incomes) {
        val list = mutableListOf<CustomerPassbookEntry>()
        works.forEach { list.add(CustomerPassbookEntry.WorkEntry(it)) }
        incomes.forEach { list.add(CustomerPassbookEntry.PaymentEntry(it)) }
        list.sortedByDescending { it.timestamp }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(liveCustomer.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            "Customer Passbook & Ledger",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (liveCustomer.mobileNumber.isNotBlank()) {
                        IconButton(onClick = {
                            CustomerShareUtils.makeCall(context, liveCustomer.mobileNumber)
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "Call Customer", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        IconButton(onClick = {
                            CustomerShareUtils.openWhatsApp(
                                context = context,
                                mobileNumber = liveCustomer.mobileNumber,
                                customerName = liveCustomer.name,
                                totalAmount = liveCustomer.totalBilled,
                                pendingAmount = liveCustomer.pendingBalance
                            )
                        }) {
                            Icon(Icons.Default.ChatBubble, contentDescription = "WhatsApp Bill", tint = ProfitGreen)
                        }
                        IconButton(onClick = {
                            CustomerShareUtils.openSms(
                                context = context,
                                mobileNumber = liveCustomer.mobileNumber,
                                customerName = liveCustomer.name,
                                totalAmount = liveCustomer.totalBilled,
                                pendingAmount = liveCustomer.pendingBalance
                            )
                        }) {
                            Icon(Icons.Default.Sms, contentDescription = "SMS Bill", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showPaymentDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Receive Payment") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Customer Summary Card (Total Billed, Received, Remaining)
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
                                "Remaining Balance",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "₹${"%.2f".format(liveCustomer.pendingBalance)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (liveCustomer.pendingBalance > 0) ExpenseRed else ProfitGreen
                            )
                        }

                        if (liveCustomer.pendingBalance > 0) {
                            Button(
                                onClick = { showPaymentDialog = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pay", fontSize = 12.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Billed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${"%.2f".format(liveCustomer.totalBilled)}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Received", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${"%.2f".format(liveCustomer.totalPaid)}", fontWeight = FontWeight.SemiBold, color = ProfitGreen, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // WhatsApp-style Transaction History Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Transaction History (${entries.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No work or payment records found for this customer.",
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
                    items(entries) { entry ->
                        CustomerPassbookBubble(entry, liveCustomer)
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        RecordCustomerPaymentDialog(
            customer = liveCustomer,
            viewModel = viewModel,
            onDismiss = { showPaymentDialog = false }
        )
    }
}

@Composable
fun CustomerPassbookBubble(
    entry: CustomerPassbookEntry,
    customer: CustomerEntity
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    when (entry) {
        is CustomerPassbookEntry.WorkEntry -> {
            val work = entry.work
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
                            .size(40.dp)
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
                                text = "Work Order / Billed",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "+ ₹${"%.2f".format(work.totalWorkAmount)}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        val workDesc = if (work.totalTimeHours <= 0.0) {
                            "Fixed Price Work • ${work.location.ifBlank { "Site" }}"
                        } else {
                            "${"%.1f".format(work.totalTimeHours)} hrs @ ₹${"%.0f".format(work.hourlyRate)}/hr • ${work.location.ifBlank { "Site" }}"
                        }

                        Text(
                            text = workDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormat.format(Date(work.date)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 10.5.sp
                            )

                            if (customer.mobileNumber.isNotBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            CustomerShareUtils.openWhatsApp(
                                                context = context,
                                                mobileNumber = customer.mobileNumber,
                                                customerName = customer.name,
                                                workName = work.workName,
                                                dateMillis = work.date,
                                                totalAmount = work.totalWorkAmount,
                                                pendingAmount = customer.pendingBalance
                                            )
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ChatBubble, contentDescription = "WhatsApp Bill", tint = ProfitGreen, modifier = Modifier.size(15.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            CustomerShareUtils.openSms(
                                                context = context,
                                                mobileNumber = customer.mobileNumber,
                                                customerName = customer.name,
                                                workName = work.workName,
                                                dateMillis = work.date,
                                                totalAmount = work.totalWorkAmount,
                                                pendingAmount = customer.pendingBalance
                                            )
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Sms, contentDescription = "SMS Bill", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        is CustomerPassbookEntry.PaymentEntry -> {
            val income = entry.income
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ProfitGreen.copy(alpha = 0.08f)),
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ProfitGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ProfitGreen,
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
                                text = "Payment Received",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ProfitGreen
                            )
                            Text(
                                text = "- ₹${"%.2f".format(income.paidAmount)}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ProfitGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        val refText = if (income.transactionReferenceId.isNotBlank()) "Ref: ${income.transactionReferenceId}" else income.paymentMode.name
                        Text(
                            text = "Mode: ${income.paymentMode.name} • $refText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = dateFormat.format(Date(income.date)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 10.5.sp
                        )
                    }
                }
            }
        }
    }
}
