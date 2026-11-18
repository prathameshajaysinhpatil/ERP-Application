package com.example.balaji_earthmovers.ui.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.balaji_earthmovers.R
import com.example.balaji_earthmovers.data.local.entity.CustomerEntity
import com.example.balaji_earthmovers.data.local.entity.DriverEntity
import com.example.balaji_earthmovers.ui.screens.AddExpenseDialog
import com.example.balaji_earthmovers.ui.screens.AppLockScreen
import com.example.balaji_earthmovers.ui.screens.AppSplashScreen
import com.example.balaji_earthmovers.ui.screens.CustomerAgingScreen
import com.example.balaji_earthmovers.ui.screens.CustomerPassbookScreen
import com.example.balaji_earthmovers.ui.screens.DashboardScreen
import com.example.balaji_earthmovers.ui.screens.DirectoryScreen
import com.example.balaji_earthmovers.ui.screens.DriverPassbookScreen
import com.example.balaji_earthmovers.ui.screens.FleetWorkScreen
import com.example.balaji_earthmovers.ui.screens.LedgerScreen
import com.example.balaji_earthmovers.ui.screens.ReportsScreen
import com.example.balaji_earthmovers.ui.screens.SettingsScreen
import com.example.balaji_earthmovers.ui.screens.WorkEntryDialog
import com.example.balaji_earthmovers.ui.util.AppStrings
import com.example.balaji_earthmovers.ui.util.LocalStrings
import com.example.balaji_earthmovers.ui.viewmodel.BalajiViewModel
import kotlinx.coroutines.flow.collectLatest

sealed class NavItem(val route: String, val icon: ImageVector) {
    object Dashboard : NavItem("dashboard", Icons.Default.Home)
    object Vehicle : NavItem("vehicle", Icons.Default.DirectionsCar)
    object Work : NavItem("work", Icons.Default.Assignment)
    object Customer : NavItem("customer", Icons.Default.People)
    object Driver : NavItem("driver", Icons.Default.Person)
    object Report : NavItem("report", Icons.Default.Assessment)
}

@Composable
fun MainErpApp(
    viewModel: BalajiViewModel = viewModel()
) {
    val currentLanguage by viewModel.language.collectAsState()
    val strings = remember(currentLanguage) { AppStrings.forLanguage(currentLanguage) }

    var isSplashDone by remember { mutableStateOf(false) }
    var isAppUnlocked by remember { mutableStateOf(false) }
    var isViewingSettings by remember { mutableStateOf(false) }
    var isViewingLedger by remember { mutableStateOf(false) }

    var currentTab by remember { mutableStateOf<NavItem>(NavItem.Dashboard) }
    var viewingPassbookDriver by remember { mutableStateOf<DriverEntity?>(null) }
    var viewingPassbookCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var isViewingAgingScreen by remember { mutableStateOf(false) }

    var showWorkEntryDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var lastBackPressedTime by remember { mutableLongStateOf(0L) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen to UI messages from ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    CompositionLocalProvider(LocalStrings provides strings) {
        // Step 1: Animated Splash Screen
        if (!isSplashDone) {
            AppSplashScreen(
                onSplashFinished = { isSplashDone = true }
            )
            return@CompositionLocalProvider
        }

        // Step 2: App PIN Lock Screen
        if (!isAppUnlocked) {
            AppLockScreen(
                viewModel = viewModel,
                onUnlocked = { isAppUnlocked = true }
            )
            return@CompositionLocalProvider
        }

        // Step 3: Back button handler - navigates back logically instead of instantly closing the app
        BackHandler(enabled = true) {
            when {
                showWorkEntryDialog -> showWorkEntryDialog = false
                showExpenseDialog -> showExpenseDialog = false
                isViewingSettings -> isViewingSettings = false
                isViewingLedger -> isViewingLedger = false
                viewingPassbookDriver != null -> viewingPassbookDriver = null
                viewingPassbookCustomer != null -> viewingPassbookCustomer = null
                isViewingAgingScreen -> isViewingAgingScreen = false
                currentTab != NavItem.Dashboard -> currentTab = NavItem.Dashboard
                else -> {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressedTime < 2000L) {
                        (context as? Activity)?.finish()
                    } else {
                        lastBackPressedTime = currentTime
                        Toast.makeText(context, strings.pressBackAgainToExit, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Step 4: Screen Routing
        when {
            // Full screen Settings view
            isViewingSettings -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { isViewingSettings = false }
                )
            }
            // Full screen Ledger view (Income & Expense)
            isViewingLedger -> {
                LedgerScreen(
                    viewModel = viewModel,
                    onBack = { isViewingLedger = false }
                )
            }
            // Full screen Driver Passbook view
            viewingPassbookDriver != null -> {
                DriverPassbookScreen(
                    driver = viewingPassbookDriver!!,
                    viewModel = viewModel,
                    onBack = { viewingPassbookDriver = null }
                )
            }
            // Full screen Customer Passbook view
            viewingPassbookCustomer != null -> {
                CustomerPassbookScreen(
                    customer = viewingPassbookCustomer!!,
                    viewModel = viewModel,
                    onBack = { viewingPassbookCustomer = null }
                )
            }
            // Full screen Customer Aging / Receivables Analysis view
            isViewingAgingScreen -> {
                CustomerAgingScreen(
                    viewModel = viewModel,
                    onBack = { isViewingAgingScreen = false },
                    onOpenCustomerPassbook = { cust ->
                        viewingPassbookCustomer = cust
                    }
                )
            }
            else -> {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        BalajiTopBar(
                            onOpenSettings = { isViewingSettings = true }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            modifier = Modifier.border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            val items = listOf(
                                NavItem.Dashboard,
                                NavItem.Vehicle,
                                NavItem.Work,
                                NavItem.Customer,
                                NavItem.Driver,
                                NavItem.Report
                            )
                            items.forEach { item ->
                                val title = when (item) {
                                    NavItem.Dashboard -> strings.navDashboard
                                    NavItem.Vehicle -> strings.navVehicle
                                    NavItem.Work -> strings.navWork
                                    NavItem.Customer -> strings.navCustomer
                                    NavItem.Driver -> strings.navDriver
                                    NavItem.Report -> strings.navReports
                                }
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            item.icon,
                                            contentDescription = title,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = title,
                                            fontSize = 10.sp,
                                            fontWeight = if (currentTab == item) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    selected = currentTab == item,
                                    onClick = { currentTab = item }
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        when (currentTab) {
                            NavItem.Dashboard -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToWork = { currentTab = NavItem.Work },
                                onNavigateToFleet = { currentTab = NavItem.Vehicle },
                                onNavigateToAging = { isViewingAgingScreen = true },
                                onOpenSettings = { isViewingSettings = true },
                                onOpenNewWorkDialog = { showWorkEntryDialog = true },
                                onOpenExpenseDialog = { showExpenseDialog = true },
                                onOpenLedger = { isViewingLedger = true }
                            )
                            NavItem.Vehicle -> FleetWorkScreen(
                                viewModel = viewModel,
                                onOpenNewWorkDialog = { showWorkEntryDialog = true },
                                initialTab = 0
                            )
                            NavItem.Work -> FleetWorkScreen(
                                viewModel = viewModel,
                                onOpenNewWorkDialog = { showWorkEntryDialog = true },
                                initialTab = 1
                            )
                            NavItem.Customer -> DirectoryScreen(
                                viewModel = viewModel,
                                onSelectDriverPassbook = { driver ->
                                    viewingPassbookDriver = driver
                                },
                                onSelectCustomerPassbook = { customer ->
                                    viewingPassbookCustomer = customer
                                },
                                onNavigateToAging = { isViewingAgingScreen = true },
                                initialTab = 1
                            )
                            NavItem.Driver -> DirectoryScreen(
                                viewModel = viewModel,
                                onSelectDriverPassbook = { driver ->
                                    viewingPassbookDriver = driver
                                },
                                onSelectCustomerPassbook = { customer ->
                                    viewingPassbookCustomer = customer
                                },
                                onNavigateToAging = { isViewingAgingScreen = true },
                                initialTab = 0
                            )
                            NavItem.Report -> ReportsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }

        if (showWorkEntryDialog) {
            WorkEntryDialog(
                viewModel = viewModel,
                onDismiss = { showWorkEntryDialog = false }
            )
        }

        if (showExpenseDialog) {
            AddExpenseDialog(
                viewModel = viewModel,
                onDismiss = { showExpenseDialog = false }
            )
        }
    }
}

/**
 * Compact, unified top bar stuck to the top of the screen:
 * - Logo (38dp)
 * - "Balaji Earthmovers Kodoli" + "Owner: Ajaysinh Jaysing Patil"
 * - Settings icon
 * Appears uniformly across Work, Driver, Vehicle, Customer, Dashboard & Report.
 */
@Composable
fun BalajiTopBar(
    onOpenSettings: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_balaji_main),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "Balaji Earthmovers Kodoli",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 14.5.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Owner: Ajaysinh Jaysing Patil",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
