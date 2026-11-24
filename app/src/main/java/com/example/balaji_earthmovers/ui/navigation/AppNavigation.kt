package com.example.balaji_earthmovers.ui.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
    object Ledger : NavItem("ledger", Icons.Default.AccountBalanceWallet)
    object Report : NavItem("report", Icons.Default.Assessment)
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var showHamburgerMenu by remember { mutableStateOf(false) }

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
                showHamburgerMenu -> showHamburgerMenu = false
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
                            viewModel = viewModel,
                            onOpenSettings = { isViewingSettings = true },
                            onOpenMenu = { showHamburgerMenu = true }
                        )
                    },
                    bottomBar = {
                        // Display ONLY 4 primary tabs on main screen: Dashboard, Work, Customer, Khata
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            modifier = Modifier.border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            val primaryItems = listOf(
                                NavItem.Dashboard,
                                NavItem.Work,
                                NavItem.Customer,
                                NavItem.Ledger
                            )
                            primaryItems.forEach { item ->
                                val title = when (item) {
                                    NavItem.Dashboard -> strings.navDashboard
                                    NavItem.Work -> strings.navWork
                                    NavItem.Customer -> strings.navCustomer
                                    NavItem.Ledger -> strings.navLedgerShort
                                    else -> ""
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
                                            fontSize = 10.5.sp,
                                            fontWeight = if (currentTab == item && !showHamburgerMenu) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    selected = currentTab == item && !showHamburgerMenu,
                                    onClick = {
                                        currentTab = item
                                        showHamburgerMenu = false
                                    }
                                )
                            }

                            // 5th Navigation Item: Menu (moved down next to Khata)
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        Icons.Default.Menu,
                                        contentDescription = "Menu",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Menu",
                                        fontSize = 10.5.sp,
                                        fontWeight = if (showHamburgerMenu) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                },
                                alwaysShowLabel = true,
                                selected = showHamburgerMenu,
                                onClick = { showHamburgerMenu = true }
                            )
                        }
                    }
                ) { padding ->
                    // Horizontal swipe navigation across screens (e.g. Vehicle -> Work -> Vehicle)
                    val swipeScreens = listOf(
                        NavItem.Dashboard,
                        NavItem.Work,
                        NavItem.Customer,
                        NavItem.Ledger,
                        NavItem.Vehicle,
                        NavItem.Driver
                    )
                    var dragAccumulator by remember { mutableFloatStateOf(0f) }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .pointerInput(currentTab) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (dragAccumulator > 120f) {
                                            // Slided screen to the right -> switch to adjacent screen
                                            val idx = swipeScreens.indexOf(currentTab)
                                            if (idx > 0) {
                                                currentTab = swipeScreens[idx - 1]
                                            } else if (idx == 0) {
                                                currentTab = swipeScreens.last()
                                            }
                                        } else if (dragAccumulator < -120f) {
                                            // Slided screen to the left -> switch to adjacent screen
                                            val idx = swipeScreens.indexOf(currentTab)
                                            if (idx >= 0 && idx < swipeScreens.size - 1) {
                                                currentTab = swipeScreens[idx + 1]
                                            } else if (idx == swipeScreens.size - 1) {
                                                currentTab = swipeScreens.first()
                                            }
                                        }
                                        dragAccumulator = 0f
                                    },
                                    onDragCancel = { dragAccumulator = 0f },
                                    onHorizontalDrag = { _, dragAmount ->
                                        dragAccumulator += dragAmount
                                    }
                                )
                            }
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
                            NavItem.Ledger -> LedgerScreen(
                                viewModel = viewModel,
                                onBack = null
                            )
                            NavItem.Report -> ReportsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }

        // ==================== INSTAGRAM-STYLE 3-LINES HAMBURGER MENU ====================
        if (showHamburgerMenu) {
            ModalBottomSheet(
                onDismissRequest = { showHamburgerMenu = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                var quickMenuExpanded by remember { mutableStateOf(false) }
                var selectedQuickModule by remember { mutableStateOf("") }

                val quickModules = listOf(
                    Triple("Vehicles / गाड्या (मशिनरी)", Icons.Default.DirectionsCar) {
                        currentTab = NavItem.Vehicle
                        showHamburgerMenu = false
                    },
                    Triple("Drivers / चालक (पगार)", Icons.Default.Person) {
                        currentTab = NavItem.Driver
                        showHamburgerMenu = false
                    },
                    Triple("Customers / ग्राहक यादी", Icons.Default.People) {
                        currentTab = NavItem.Customer
                        showHamburgerMenu = false
                    },
                    Triple("Work / कामे नोंदणी", Icons.Default.Assignment) {
                        currentTab = NavItem.Work
                        showHamburgerMenu = false
                    },
                    Triple("Income / जमा (उत्पन्न)", Icons.Default.TrendingUp) {
                        currentTab = NavItem.Ledger
                        showHamburgerMenu = false
                    },
                    Triple("Expense / खर्च नोंद", Icons.Default.TrendingDown) {
                        currentTab = NavItem.Ledger
                        showHamburgerMenu = false
                    }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Balaji Earthmovers Menu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                "Quick Access",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // ==================== 100% WIDTH QUICK MENU DROPDOWN ====================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text("Quick Menu / जलद मेनू", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.primary)
                            }

                            ExposedDropdownMenuBox(
                                expanded = quickMenuExpanded,
                                onExpandedChange = { quickMenuExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedQuickModule.ifBlank { "Select Quick Module (थेट जा...)" },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = quickMenuExpanded) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = quickMenuExpanded,
                                    onDismissRequest = { quickMenuExpanded = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    quickModules.forEach { (label, icon, action) ->
                                        DropdownMenuItem(
                                            text = { Text(label, fontWeight = FontWeight.Medium) },
                                            leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            onClick = {
                                                selectedQuickModule = label
                                                quickMenuExpanded = false
                                                action()
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    // ==================== PROFESSIONAL RESPONSIVE MODULE CARDS ====================
                    val menuModules = listOf(
                        MenuCardItem(
                            icon = Icons.Default.DirectionsCar,
                            title = "Vehicles & Machinery",
                            subtitle = "JCB, Poclain, Tractors, Service hours & maintenance",
                            badge = "Fleet",
                            onClick = {
                                currentTab = NavItem.Vehicle
                                showHamburgerMenu = false
                            }
                        ),
                        MenuCardItem(
                            icon = Icons.Default.Person,
                            title = "Drivers Directory",
                            subtitle = "Driver profiles, salaries, advances & passbook",
                            badge = "Staff",
                            onClick = {
                                currentTab = NavItem.Driver
                                showHamburgerMenu = false
                            }
                        ),
                        MenuCardItem(
                            icon = Icons.Default.Assessment,
                            title = "Business Reports",
                            subtitle = "Filtered vehicle, driver & customer statements",
                            badge = "Reports",
                            onClick = {
                                currentTab = NavItem.Report
                                showHamburgerMenu = false
                            }
                        ),
                        MenuCardItem(
                            icon = Icons.Default.Storage,
                            title = "Data, Storage & Backup",
                            subtitle = "Export JSON, import database, secure local backup",
                            badge = "Backup",
                            onClick = {
                                showHamburgerMenu = false
                                isViewingSettings = true
                            }
                        ),
                        MenuCardItem(
                            icon = Icons.Default.Settings,
                            title = "App Settings & Profile",
                            subtitle = "Business name, owner info, themes & security PIN",
                            badge = "Config",
                            onClick = {
                                showHamburgerMenu = false
                                isViewingSettings = true
                            }
                        ),
                        MenuCardItem(
                            icon = Icons.Default.Lock,
                            title = "Lock App",
                            subtitle = "Instantly secure session with PIN protection",
                            badge = "Security",
                            onClick = {
                                showHamburgerMenu = false
                                isAppUnlocked = false
                            }
                        )
                    )

                    menuModules.forEach { item ->
                        ProfessionalMenuCard(item)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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
 * - Dynamic Business Name + Owner Name
 * - Settings icon + Instagram-style 3 horizontal lines (Menu)
 */
@Composable
fun BalajiTopBar(
    viewModel: BalajiViewModel,
    onOpenSettings: () -> Unit,
    onOpenMenu: () -> Unit
) {
    val businessName by viewModel.businessName.collectAsState()
    val ownerName by viewModel.ownerName.collectAsState()

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
                        text = businessName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 14.5.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Owner: $ownerName",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                IconButton(
                    onClick = onOpenMenu,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

data class MenuCardItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val badge: String,
    val onClick: () -> Unit
)

@Composable
fun ProfessionalMenuCard(item: MenuCardItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                item.badge,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
