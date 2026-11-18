package com.example.balaji_earthmovers.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balaji_earthmovers.R
import com.example.balaji_earthmovers.data.local.entity.VehicleEntity
import com.example.balaji_earthmovers.data.local.entity.WorkWithDetails
import com.example.balaji_earthmovers.ui.theme.AmberPrimary
import com.example.balaji_earthmovers.ui.theme.ExpenseRed
import com.example.balaji_earthmovers.ui.theme.InfoBlue
import com.example.balaji_earthmovers.ui.theme.ProfitGreen
import com.example.balaji_earthmovers.ui.util.LocalStrings
import com.example.balaji_earthmovers.ui.viewmodel.BalajiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BalajiViewModel,
    onNavigateToWork: () -> Unit,
    onNavigateToFleet: () -> Unit,
    onNavigateToAging: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNewWorkDialog: () -> Unit,
    onOpenExpenseDialog: () -> Unit,
    onOpenLedger: () -> Unit = {}
) {
    val strings = LocalStrings.current
    val netProfit by viewModel.netProfit.collectAsState()
    val totalRevenue by viewModel.totalCollectedIncome.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val totalBilled by viewModel.totalWorkBilled.collectAsState()
    val pendingReceivables by viewModel.totalPendingReceivables.collectAsState()
    val vehicles by viewModel.allVehicles.collectAsState()
    val vehiclesDueForService by viewModel.vehiclesDueForService.collectAsState()
    val recentWorks by viewModel.allWorks.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenNewWorkDialog,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.newWork)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Service Alert if overdue
            if (vehiclesDueForService.isNotEmpty()) {
                item {
                    ServiceAlertBanner(
                        vehicles = vehiclesDueForService,
                        onMarkServiced = { vehicleId ->
                            viewModel.markVehicleServiced(vehicleId)
                        },
                        onViewFleet = onNavigateToFleet
                    )
                }
            }

            // Quick Operations / Quick Apps - Aligned in uniform equal height and width
            item {
                Text(
                    strings.quickActions,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            title = strings.newWork,
                            subtitle = strings.tabWork,
                            icon = Icons.Default.Add,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f)
                                .height(78.dp),
                            onClick = onOpenNewWorkDialog
                        )

                        QuickActionCard(
                            title = strings.addExpense,
                            subtitle = strings.tabExpense,
                            icon = Icons.Default.MoneyOff,
                            color = ExpenseRed,
                            modifier = Modifier
                                .weight(1f)
                                .height(78.dp),
                            onClick = onOpenExpenseDialog
                        )
                    }

                    // Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            title = strings.pendingToCollect,
                            subtitle = strings.agingAnalysis,
                            icon = Icons.Default.Payment,
                            color = AmberPrimary,
                            modifier = Modifier
                                .weight(1f)
                                .height(78.dp),
                            onClick = onNavigateToAging
                        )

                        QuickActionCard(
                            title = strings.tabVehicles,
                            subtitle = "${vehicles.size} ${strings.tabVehicles}",
                            icon = Icons.Default.DriveEta,
                            color = InfoBlue,
                            modifier = Modifier
                                .weight(1f)
                                .height(78.dp),
                            onClick = onNavigateToFleet
                        )
                    }
                }
            }

            // Overview Section
            item {
                Text(
                    strings.navDashboard,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Net Balance Hero Card (Total Received - Total Spent)
            item {
                MetricCard(
                    title = "Net Balance (${strings.netEarnings})",
                    value = "₹${"%.2f".format(netProfit)}",
                    subtitle = "Received: ₹${"%.0f".format(totalRevenue)}  |  Spent: ₹${"%.0f".format(totalExpenses)}",
                    color = if (netProfit >= 0) ProfitGreen else ExpenseRed,
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Row 1: Gross Earnings (Work Billed) & Total Received (Income Collected)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Gross Earnings",
                        value = "₹${"%.2f".format(totalBilled)}",
                        subtitle = "Total Work Billed",
                        color = InfoBlue,
                        icon = Icons.Default.Assignment,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWork
                    )

                    MetricCard(
                        title = strings.totalReceived,
                        value = "₹${"%.2f".format(totalRevenue)}",
                        subtitle = "Total Incomes Received",
                        color = ProfitGreen,
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenLedger
                    )
                }
            }

            // Row 2: Pending from Clients & Total Spend
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = strings.pendingToCollect,
                        value = "₹${"%.2f".format(pendingReceivables)}",
                        subtitle = "Client Outstanding",
                        color = if (pendingReceivables > 0) ExpenseRed else ProfitGreen,
                        icon = Icons.Default.CurrencyRupee,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAging
                    )

                    MetricCard(
                        title = strings.totalSpent,
                        value = "₹${"%.2f".format(totalExpenses)}",
                        subtitle = "Expenses & Spares",
                        color = ExpenseRed,
                        icon = Icons.Default.MoneyOff,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenLedger
                    )
                }
            }

            item {
                MetricCard(
                    title = strings.tabVehicles,
                    value = "${vehicles.size} ${strings.tabVehicles}",
                    subtitle = if (vehiclesDueForService.isNotEmpty()) "${vehiclesDueForService.size} ${strings.serviceAlert}" else strings.statusCompleted,
                    color = if (vehiclesDueForService.isNotEmpty()) ExpenseRed else InfoBlue,
                    icon = Icons.Default.DriveEta,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToFleet
                )
            }

            // Recent Work
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        strings.recentWork,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        strings.viewAll,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToWork() }
                    )
                }
            }

            if (recentWorks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(strings.noRecentWork)
                        }
                    }
                }
            } else {
                items(recentWorks.take(5), key = { it.work.id }) { workWithDetails ->
                    RecentWorkItemCard(workWithDetails)
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.14f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Column(verticalArrangement = Arrangement.Center) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ServiceAlertBanner(
    vehicles: List<VehicleEntity>,
    onMarkServiced: (String) -> Unit,
    onViewFleet: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ExpenseRed)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(20.dp))
                    Text(
                        "Service Alert (${vehicles.size} Due)",
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Text(
                    "View All",
                    style = MaterialTheme.typography.labelMedium,
                    color = ExpenseRed,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onViewFleet() }
                )
            }

            Text(
                "These machines have reached 250 work hours and need service:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            vehicles.take(2).forEach { vehicle ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column {
                        Text(
                            "${vehicle.vehicleName} (${vehicle.vehicleNumber})",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "${"%.1f".format(vehicle.hoursSinceLastService)} hrs since last service (Limit: 250h)",
                            style = MaterialTheme.typography.labelSmall,
                            color = ExpenseRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { onMarkServiced(vehicle.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset 250h Service", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.12f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )

            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecentWorkItemCard(workWithDetails: WorkWithDetails) {
    val work = workWithDetails.work
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
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(work.workName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Customer: ${workWithDetails.customer.name} | Vehicle: ${workWithDetails.vehicle.vehicleName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Driver: ${workWithDetails.driver.driverName} • $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${"%.2f".format(work.totalWorkAmount)}",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = ProfitGreen
                )
                Text(
                    "${work.totalTimeHours} hrs @ ₹${work.hourlyRate}/h",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
