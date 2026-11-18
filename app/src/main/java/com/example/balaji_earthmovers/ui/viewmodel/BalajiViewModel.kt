package com.example.balaji_earthmovers.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.balaji_earthmovers.data.local.AppDatabase
import com.example.balaji_earthmovers.data.local.backup.BackupRestoreManager
import com.example.balaji_earthmovers.data.local.entity.CustomerAgingSummary
import com.example.balaji_earthmovers.data.local.entity.CustomerEntity
import com.example.balaji_earthmovers.data.local.entity.CustomerWithHistory
import com.example.balaji_earthmovers.data.local.entity.DriverEntity
import com.example.balaji_earthmovers.data.local.entity.DriverTransactionEntity
import com.example.balaji_earthmovers.data.local.entity.DriverTransactionType
import com.example.balaji_earthmovers.data.local.entity.ExpenseEntity
import com.example.balaji_earthmovers.data.local.entity.ExpenseType
import com.example.balaji_earthmovers.data.local.entity.IncomeEntity
import com.example.balaji_earthmovers.data.local.entity.PaymentMode
import com.example.balaji_earthmovers.data.local.entity.VehicleEntity
import com.example.balaji_earthmovers.data.local.entity.WorkEntity
import com.example.balaji_earthmovers.data.local.entity.WorkWithDetails
import com.example.balaji_earthmovers.data.repository.BalajiRepository
import com.example.balaji_earthmovers.data.prefs.PreferencesManager
import com.example.balaji_earthmovers.data.prefs.AppThemeMode
import com.example.balaji_earthmovers.data.prefs.AppLanguage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class FleetSeasonFilter(val displayName: String) {
    ALL("All Time"),
    THIS_MONTH("This Month"),
    MONSOON("Monsoon (Jun-Sep)"),
    PEAK_CONSTRUCTION("Peak Season (Oct-May)")
}

// --- FORM STATES WITH MATERIAL 3 VALIDATION ---
data class WorkFormState(
    val workName: String = "",
    val workNameError: String? = null,
    val selectedVehicleId: String = "",
    val vehicleError: String? = null,
    val selectedDriverId: String = "",
    val driverError: String? = null,
    val selectedCustomerId: String = "",
    val customerError: String? = null,
    val isFixedAmount: Boolean = false,
    val fixedAmount: String = "",
    val fixedAmountError: String? = null,
    val totalHours: String = "",
    val totalHoursError: String? = null,
    val hourlyRate: String = "",
    val hourlyRateError: String? = null,
    val initialPaidAmount: String = "0",
    val initialPaidError: String? = null,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val transactionRef: String = "",
    val location: String = ""
) {
    val totalAmount: Double
        get() = if (isFixedAmount) {
            fixedAmount.toDoubleOrNull() ?: 0.0
        } else {
            val h = totalHours.toDoubleOrNull() ?: 0.0
            val r = hourlyRate.toDoubleOrNull() ?: 0.0
            h * r
        }

    val isValid: Boolean
        get() = workName.isNotBlank() &&
                selectedVehicleId.isNotBlank() &&
                selectedDriverId.isNotBlank() &&
                selectedCustomerId.isNotBlank() &&
                (if (isFixedAmount) (fixedAmount.toDoubleOrNull() ?: 0.0) > 0.0 else (totalHours.toDoubleOrNull() ?: 0.0) > 0.0 && (hourlyRate.toDoubleOrNull() ?: 0.0) >= 0.0) &&
                workNameError == null &&
                vehicleError == null &&
                driverError == null &&
                customerError == null &&
                (if (isFixedAmount) fixedAmountError == null else totalHoursError == null && hourlyRateError == null) &&
                initialPaidError == null
}

data class ExpenseFormState(
    val expenseName: String = "",
    val expenseNameError: String? = null,
    val expenseType: ExpenseType = ExpenseType.FUEL,
    val amount: String = "",
    val amountError: String? = null,
    val description: String = "",
    val descriptionError: String? = null,
    val linkedDriverId: String? = null,
    val driverError: String? = null,
    val linkedVehicleId: String? = null,
    val vehicleError: String? = null
) {
    val isValid: Boolean
        get() {
            val parsedAmount = amount.toDoubleOrNull() ?: 0.0
            if (expenseName.isBlank() || parsedAmount <= 0.0) return false
            if (expenseType == ExpenseType.OTHER && description.isBlank()) return false
            if (expenseType == ExpenseType.SALARY && linkedDriverId.isNullOrBlank()) return false
            if ((expenseType == ExpenseType.FUEL || expenseType == ExpenseType.MAINTENANCE) && linkedVehicleId.isNullOrBlank()) return false
            return expenseNameError == null && amountError == null && descriptionError == null && driverError == null && vehicleError == null
        }
}

data class DriverPayoutFormState(
    val amount: String = "",
    val amountError: String? = null,
    val transactionType: DriverTransactionType = DriverTransactionType.SALARY,
    val notes: String = ""
) {
    val isValid: Boolean
        get() = (amount.toDoubleOrNull() ?: 0.0) > 0.0 && amountError == null
}

data class DriverFormState(
    val name: String = "",
    val nameError: String? = null,
    val mobile: String = "",
    val mobileError: String? = null,
    val address: String = "",
    val addressError: String? = null,
    val aadhaar: String = "",
    val aadhaarError: String? = null,
    val monthlySalary: String = "",
    val salaryError: String? = null,
    val assignedVehicleId: String? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
                mobile.matches(Regex("^[6-9]\\d{9}$")) &&
                (aadhaar.isBlank() || aadhaar.matches(Regex("^\\d{12}$")) || aadhaar.length > 12) &&
                (monthlySalary.toDoubleOrNull() ?: -1.0) >= 0.0 &&
                nameError == null && mobileError == null && aadhaarError == null && salaryError == null
}

data class CustomerFormState(
    val name: String = "",
    val nameError: String? = null,
    val mobile: String = "",
    val mobileError: String? = null,
    val address: String = "",
    val gstin: String = "",
    val gstinError: String? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
                mobile.matches(Regex("^[6-9]\\d{9}$")) &&
                (gstin.isBlank() || gstin.matches(Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$"))) &&
                nameError == null && mobileError == null && gstinError == null
}

data class VehicleFormState(
    val name: String = "",
    val nameError: String? = null,
    val vehicleNumber: String = "",
    val numberError: String? = null,
    val model: String = "",
    val initialHours: String = "0.0",
    val hoursError: String? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank() && vehicleNumber.isNotBlank() &&
                (initialHours.toDoubleOrNull() ?: -1.0) >= 0.0 &&
                nameError == null && numberError == null && hoursError == null
}

data class PaymentReceiptFormState(
    val customerId: String = "",
    val amount: String = "",
    val amountError: String? = null,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val transactionRef: String = ""
) {
    val isValid: Boolean
        get() = customerId.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0.0 && amountError == null
}

class BalajiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BalajiRepository(AppDatabase.getInstance(application))
    private val prefsManager = PreferencesManager.getInstance(application)

    val themeMode: StateFlow<AppThemeMode> = prefsManager.themeMode
    val language: StateFlow<AppLanguage> = prefsManager.language
    val appPin: StateFlow<String> = prefsManager.appPin
    val adminPin: StateFlow<String> = prefsManager.adminPin

    fun setThemeMode(mode: AppThemeMode) = prefsManager.setThemeMode(mode)
    fun setAppPin(pin: String) = prefsManager.setAppPin(pin)
    fun setAdminPin(pin: String) = prefsManager.setAdminPin(pin)
    fun setLanguage(lang: AppLanguage) = prefsManager.setLanguage(lang)
    fun verifyAppPin(pin: String): Boolean = prefsManager.verifyAppPin(pin)
    fun verifyAdminPin(pin: String): Boolean = prefsManager.verifyAdminPin(pin)

    // UI Message Events (Snackbars, Alerts)
    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    // --- DATA STREAMS ---
    val allVehicles: StateFlow<List<VehicleEntity>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vehiclesDueForService: StateFlow<List<VehicleEntity>> = repository.vehiclesDueForService
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDrivers: StateFlow<List<DriverEntity>> = repository.allDrivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorks: StateFlow<List<WorkWithDetails>> = repository.allWorksWithDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allIncomes: StateFlow<List<IncomeEntity>> = repository.allIncomes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPendingReceivables: StateFlow<Double> = repository.totalPendingReceivables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalWorkBilled: StateFlow<Double> = repository.totalWorkBilled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val customersAgingSummary: StateFlow<List<CustomerAgingSummary>> = repository.getAllCustomersAgingSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Overview: Net Profit = Total Collected Incomes - Total Expenses
    val netProfit: StateFlow<Double> = combine(
        repository.totalCollectedIncome,
        repository.totalExpenses
    ) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalCollectedIncome: StateFlow<Double> = repository.totalCollectedIncome
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double> = repository.totalExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- SEASONAL FILTER ---
    private val _seasonFilter = MutableStateFlow(FleetSeasonFilter.ALL)
    val seasonFilter: StateFlow<FleetSeasonFilter> = _seasonFilter.asStateFlow()

    fun setSeasonFilter(filter: FleetSeasonFilter) {
        _seasonFilter.value = filter
    }

    val filteredVehicles: StateFlow<List<VehicleEntity>> = combine(
        allVehicles,
        _seasonFilter
    ) { vehicles, _ ->
        vehicles
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- FORM STATE HOLDERS ---
    private val _workFormState = MutableStateFlow(WorkFormState())
    val workFormState: StateFlow<WorkFormState> = _workFormState.asStateFlow()

    private val _expenseFormState = MutableStateFlow(ExpenseFormState())
    val expenseFormState: StateFlow<ExpenseFormState> = _expenseFormState.asStateFlow()

    private val _driverPayoutFormState = MutableStateFlow(DriverPayoutFormState())
    val driverPayoutFormState: StateFlow<DriverPayoutFormState> = _driverPayoutFormState.asStateFlow()

    private val _driverFormState = MutableStateFlow(DriverFormState())
    val driverFormState: StateFlow<DriverFormState> = _driverFormState.asStateFlow()

    private val _customerFormState = MutableStateFlow(CustomerFormState())
    val customerFormState: StateFlow<CustomerFormState> = _customerFormState.asStateFlow()

    private val _vehicleFormState = MutableStateFlow(VehicleFormState())
    val vehicleFormState: StateFlow<VehicleFormState> = _vehicleFormState.asStateFlow()

    private val _paymentFormState = MutableStateFlow(PaymentReceiptFormState())
    val paymentFormState: StateFlow<PaymentReceiptFormState> = _paymentFormState.asStateFlow()

    // --- FORM FIELD UPDATERS & VALIDATORS ---
    fun updateWorkForm(update: (WorkFormState) -> WorkFormState) {
        val newState = update(_workFormState.value)
        val hoursVal = newState.totalHours.toDoubleOrNull()
        val rateVal = newState.hourlyRate.toDoubleOrNull()
        val fixedVal = newState.fixedAmount.toDoubleOrNull()
        val paidVal = newState.initialPaidAmount.toDoubleOrNull()

        _workFormState.value = newState.copy(
            workNameError = if (newState.workName.isNotBlank() && newState.workName.length < 3) "Work name must be at least 3 chars" else null,
            fixedAmountError = if (newState.isFixedAmount && newState.fixedAmount.isNotBlank() && (fixedVal == null || fixedVal <= 0)) "Fixed amount must be > 0" else null,
            totalHoursError = if (!newState.isFixedAmount && newState.totalHours.isNotBlank() && (hoursVal == null || hoursVal <= 0)) "Must be greater than 0 hrs" else null,
            hourlyRateError = if (!newState.isFixedAmount && newState.hourlyRate.isNotBlank() && (rateVal == null || rateVal < 0)) "Invalid rate" else null,
            initialPaidError = if (newState.initialPaidAmount.isNotBlank() && (paidVal == null || paidVal < 0)) "Invalid paid amount" else null
        )
    }

    fun updateExpenseForm(update: (ExpenseFormState) -> ExpenseFormState) {
        val newState = update(_expenseFormState.value)
        val amountVal = newState.amount.toDoubleOrNull()

        val amountError = if (newState.amount.isNotBlank() && (amountVal == null || amountVal <= 0)) "Amount must be positive" else null
        val descError = if (newState.expenseType == ExpenseType.OTHER && newState.description.isBlank()) "Description is required for OTHER" else null
        val driverError = if (newState.expenseType == ExpenseType.SALARY && newState.linkedDriverId.isNullOrBlank()) "Driver is required for SALARY" else null
        val vehicleError = if ((newState.expenseType == ExpenseType.FUEL || newState.expenseType == ExpenseType.MAINTENANCE) && newState.linkedVehicleId.isNullOrBlank()) "Vehicle is required for FUEL/MAINTENANCE" else null

        _expenseFormState.value = newState.copy(
            amountError = amountError,
            descriptionError = descError,
            driverError = driverError,
            vehicleError = vehicleError
        )
    }

    fun updateDriverPayoutForm(update: (DriverPayoutFormState) -> DriverPayoutFormState) {
        val newState = update(_driverPayoutFormState.value)
        val amt = newState.amount.toDoubleOrNull()
        _driverPayoutFormState.value = newState.copy(
            amountError = if (newState.amount.isNotBlank() && (amt == null || amt <= 0)) "Enter a valid positive amount" else null
        )
    }

    fun updateDriverForm(update: (DriverFormState) -> DriverFormState) {
        val newState = update(_driverFormState.value)
        val salaryVal = newState.monthlySalary.toDoubleOrNull()

        _driverFormState.value = newState.copy(
            mobileError = if (newState.mobile.isNotBlank() && !newState.mobile.matches(Regex("^[6-9]\\d{9}$"))) "Enter valid 10-digit mobile (starts with 6-9)" else null,
            aadhaarError = if (newState.aadhaar.isNotBlank() && newState.aadhaar.length == 12 && !newState.aadhaar.matches(Regex("^\\d{12}$"))) "Aadhaar must be 12 digits" else null,
            salaryError = if (newState.monthlySalary.isNotBlank() && (salaryVal == null || salaryVal < 0)) "Salary must be non-negative" else null
        )
    }

    fun updateCustomerForm(update: (CustomerFormState) -> CustomerFormState) {
        val newState = update(_customerFormState.value)
        _customerFormState.value = newState.copy(
            mobileError = if (newState.mobile.isNotBlank() && !newState.mobile.matches(Regex("^[6-9]\\d{9}$"))) "Enter valid 10-digit mobile" else null,
            gstinError = if (newState.gstin.isNotBlank() && !newState.gstin.matches(Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$"))) "Invalid 15-character GSTIN format" else null
        )
    }

    fun updateVehicleForm(update: (VehicleFormState) -> VehicleFormState) {
        val newState = update(_vehicleFormState.value)
        val hoursVal = newState.initialHours.toDoubleOrNull()
        _vehicleFormState.value = newState.copy(
            numberError = if (newState.vehicleNumber.isNotBlank() && newState.vehicleNumber.length < 5) "Enter valid registration number" else null,
            hoursError = if (newState.initialHours.isNotBlank() && (hoursVal == null || hoursVal < 0)) "Hours must be non-negative" else null
        )
    }

    fun updatePaymentForm(update: (PaymentReceiptFormState) -> PaymentReceiptFormState) {
        val newState = update(_paymentFormState.value)
        val amt = newState.amount.toDoubleOrNull()
        _paymentFormState.value = newState.copy(
            amountError = if (newState.amount.isNotBlank() && (amt == null || amt <= 0)) "Enter positive payment amount" else null
        )
    }

    // --- CREATE & ATOMIC TRANSACTIONS ---

    fun submitWorkEntry(onSuccess: () -> Unit) {
        val state = _workFormState.value
        if (!state.isValid) {
            viewModelScope.launch { _uiEvents.emit("Please fix form errors before submitting") }
            return
        }

        viewModelScope.launch {
            try {
                val hours = state.totalHours.toDoubleOrNull() ?: 0.0
                val totalAmount = if (state.isFixedAmount) {
                    state.fixedAmount.toDouble()
                } else {
                    hours * (state.hourlyRate.toDoubleOrNull() ?: 0.0)
                }
                val rate = if (state.isFixedAmount) {
                    if (hours > 0) totalAmount / hours else 0.0
                } else {
                    state.hourlyRate.toDoubleOrNull() ?: 0.0
                }
                val paid = state.initialPaidAmount.toDoubleOrNull() ?: 0.0
                val now = System.currentTimeMillis()

                val work = WorkEntity(
                    workName = state.workName.trim(),
                    vehicleId = state.selectedVehicleId,
                    driverId = state.selectedDriverId,
                    customerId = state.selectedCustomerId,
                    date = now,
                    startTime = now - (hours.coerceAtLeast(1.0) * 3600 * 1000).toLong(),
                    endTime = now,
                    totalTimeHours = hours,
                    hourlyRate = rate,
                    totalWorkAmount = totalAmount,
                    location = state.location.trim()
                )

                repository.createWorkAtomic(
                    work = work,
                    initialPaidAmount = paid,
                    paymentMode = state.paymentMode,
                    referenceId = state.transactionRef.trim()
                )

                _workFormState.value = WorkFormState()
                _uiEvents.emit("Work recorded successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit("Failed to record work: ${e.localizedMessage}")
            }
        }
    }

    fun submitExpense(onSuccess: () -> Unit) {
        val state = _expenseFormState.value
        if (!state.isValid) {
            viewModelScope.launch { _uiEvents.emit("Please complete all required fields correctly") }
            return
        }

        viewModelScope.launch {
            try {
                val amt = state.amount.toDouble()
                val expense = ExpenseEntity(
                    expenseName = state.expenseName.trim(),
                    expenseType = state.expenseType,
                    description = state.description.trim(),
                    amount = amt,
                    date = System.currentTimeMillis(),
                    linkedDriverId = if (state.expenseType == ExpenseType.SALARY) state.linkedDriverId else null,
                    linkedVehicleId = if (state.expenseType == ExpenseType.FUEL || state.expenseType == ExpenseType.MAINTENANCE) state.linkedVehicleId else null
                )

                repository.recordExpenseAtomic(
                    expense = expense,
                    driverNotes = "Expense Payout: ${state.expenseName.trim()}"
                )

                _expenseFormState.value = ExpenseFormState()
                _uiEvents.emit("Expense logged successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit("Failed to log expense: ${e.localizedMessage}")
            }
        }
    }

    fun submitDriverPayout(driverId: String, onSuccess: () -> Unit) {
        val state = _driverPayoutFormState.value
        if (!state.isValid) {
            viewModelScope.launch { _uiEvents.emit("Please enter a valid amount") }
            return
        }

        viewModelScope.launch {
            try {
                val amt = state.amount.toDouble()
                val tx = DriverTransactionEntity(
                    driverId = driverId,
                    amount = amt,
                    transactionType = state.transactionType,
                    date = System.currentTimeMillis(),
                    notes = state.notes.trim().ifEmpty { "${state.transactionType.name} Payout" }
                )
                repository.addDriverPayout(tx)
                _driverPayoutFormState.value = DriverPayoutFormState()
                _uiEvents.emit("Payment recorded in passbook!")
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit("Error recording payout: ${e.localizedMessage}")
            }
        }
    }

    fun submitDriver(onSuccess: () -> Unit) {
        val state = _driverFormState.value
        if (!state.isValid) {
            viewModelScope.launch { _uiEvents.emit("Please check driver details") }
            return
        }

        viewModelScope.launch {
            try {
                val driver = DriverEntity(
                    driverName = state.name.trim(),
                    mobileNumber = state.mobile.trim(),
                    address = state.address.trim(),
                    adharNumber = state.aadhaar.trim(),
                    monthlySalary = state.monthlySalary.toDoubleOrNull() ?: 0.0,
                    joiningDate = System.currentTimeMillis(),
                    assignedVehicleId = state.assignedVehicleId
                )
                repository.insertDriver(driver)
                _driverFormState.value = DriverFormState()
                _uiEvents.emit("Driver saved successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit("Error adding driver: ${e.localizedMessage}")
            }
        }
    }

    fun submitCustomer(onSuccess: () -> Unit) {
        val state = _customerFormState.value
        if (!state.isValid) {
            viewModelScope.launch { _uiEvents.emit("Please check customer details") }
            return
        }

        viewModelScope.launch {
            try {
                val customer = CustomerEntity(
                    name = state.name.trim(),
                    mobileNumber = state.mobile.trim(),
                    address = state.address.trim(),
                    gstin = state.gstin.trim().uppercase()
                )
                repository.insertCustomer(customer)
                _customerFormState.value = CustomerFormState()
                _uiEvents.emit("Customer saved successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit("Error adding customer: ${e.localizedMessage}")
            }
        }
    }

    fun submitVehicle(vehicle: VehicleEntity? = null, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val toInsert = if (vehicle != null) {
                    vehicle
                } else {
                    val state = _vehicleFormState.value
                    if (!state.isValid) {
                        _uiEvents.emit("Please check vehicle details")
                        return@launch
                    }
                    val hours = state.initialHours.toDoubleOrNull() ?: 0.0
                    VehicleEntity(
                        vehicleName = state.name.trim(),
                        vehicleNumber = state.vehicleNumber.trim().uppercase(),
                        model = state.model.trim(),
                        workedHours = hours,
                        lastServiceHours = hours,
                        purchaseDate = System.currentTimeMillis()
                    )
                }
                repository.insertVehicle(toInsert)
                _vehicleFormState.value = VehicleFormState()
                _uiEvents.emit("Vehicle saved successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit("Error adding vehicle: ${e.localizedMessage}")
            }
        }
    }

    fun submitCustomerPayment(onSuccess: () -> Unit) {
        val state = _paymentFormState.value
        if (!state.isValid) {
            viewModelScope.launch { _uiEvents.emit("Please enter valid payment details") }
            return
        }

        viewModelScope.launch {
            try {
                val amt = state.amount.toDouble()
                val income = IncomeEntity(
                    customerId = state.customerId,
                    date = System.currentTimeMillis(),
                    totalWorkAmount = amt,
                    paidAmount = amt,
                    pendingAmount = 0.0,
                    paymentMode = state.paymentMode,
                    transactionReferenceId = state.transactionRef.trim()
                )
                repository.recordPaymentReceipt(income)
                _paymentFormState.value = PaymentReceiptFormState()
                _uiEvents.emit("Payment recorded successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit("Error recording payment: ${e.localizedMessage}")
            }
        }
    }

    // --- EDIT / UPDATE FUNCTIONS ACROSS ALL COMPONENTS ---

    fun editVehicle(vehicle: VehicleEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateVehicle(vehicle)
                _uiEvents.emit("Vehicle updated successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit("Error updating vehicle: ${e.localizedMessage}")
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                repository.softDeleteVehicle(vehicleId)
                _uiEvents.emit("Vehicle deleted.")
            } catch (e: Exception) {
                _uiEvents.emit("Error deleting vehicle: ${e.localizedMessage}")
            }
        }
    }

    fun editDriver(driver: DriverEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateDriver(driver)
                _uiEvents.emit("Driver updated successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit("Error updating driver: ${e.localizedMessage}")
            }
        }
    }

    fun deleteDriver(driverId: String) {
        viewModelScope.launch {
            try {
                repository.softDeleteDriver(driverId)
                _uiEvents.emit("Driver deleted.")
            } catch (e: Exception) {
                _uiEvents.emit("Error deleting driver: ${e.localizedMessage}")
            }
        }
    }

    fun editCustomer(customer: CustomerEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateCustomer(customer)
                _uiEvents.emit("Customer updated successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit("Error updating customer: ${e.localizedMessage}")
            }
        }
    }

    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            try {
                repository.softDeleteCustomer(customerId)
                _uiEvents.emit("Customer deleted.")
            } catch (e: Exception) {
                _uiEvents.emit("Error deleting customer: ${e.localizedMessage}")
            }
        }
    }

    fun editWork(work: WorkEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateWork(work)
                _uiEvents.emit("Work updated successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit("Error updating work: ${e.localizedMessage}")
            }
        }
    }

    fun deleteWork(workId: String) {
        viewModelScope.launch {
            try {
                repository.softDeleteWork(workId)
                _uiEvents.emit("Work deleted.")
            } catch (e: Exception) {
                _uiEvents.emit("Error deleting work: ${e.localizedMessage}")
            }
        }
    }

    fun editExpense(expense: ExpenseEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateExpense(expense)
                _uiEvents.emit("Expense updated successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit("Error updating expense: ${e.localizedMessage}")
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                repository.softDeleteExpense(expenseId)
                _uiEvents.emit("Expense deleted.")
            } catch (e: Exception) {
                _uiEvents.emit("Error deleting expense: ${e.localizedMessage}")
            }
        }
    }

    fun editIncome(income: IncomeEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateIncome(income)
                _uiEvents.emit("Income updated successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit("Error updating income: ${e.localizedMessage}")
            }
        }
    }

    fun deleteIncome(incomeId: String) {
        viewModelScope.launch {
            try {
                repository.softDeleteIncome(incomeId)
                _uiEvents.emit("Income entry deleted.")
            } catch (e: Exception) {
                _uiEvents.emit("Error deleting income: ${e.localizedMessage}")
            }
        }
    }

    fun markVehicleServiced(vehicleId: String) {
        viewModelScope.launch {
            try {
                repository.markVehicleServiced(vehicleId)
                _uiEvents.emit("Vehicle service reset to current hours.")
            } catch (e: Exception) {
                _uiEvents.emit("Error: ${e.localizedMessage}")
            }
        }
    }

    fun getDriverTransactions(driverId: String) = repository.getTransactionsForDriver(driverId)

    fun getCustomerWithHistory(customerId: String) = repository.getCustomerWithHistory(customerId)

    // --- LOCAL BACKUP & RESTORE ACTIONS ---
    fun exportBackup(onFileCreated: (File) -> Unit) {
        viewModelScope.launch {
            val result = BackupRestoreManager.exportJsonBackup(getApplication())
            result.onSuccess { file ->
                _uiEvents.emit("Backup saved to balajiearthmovers: ${file.name}")
                onFileCreated(file)
            }.onFailure { err ->
                _uiEvents.emit("Backup export failed: ${err.localizedMessage}")
            }
        }
    }

    fun importBackup(uri: Uri, onComplete: () -> Unit) {
        viewModelScope.launch {
            val result = BackupRestoreManager.importJsonBackup(getApplication(), uri)
            result.onSuccess { count ->
                _uiEvents.emit("Restored $count records into database!")
                onComplete()
            }.onFailure { err ->
                _uiEvents.emit("Backup restore failed: ${err.localizedMessage}")
            }
        }
    }

    fun clearAllData(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.clearAllData()
                _uiEvents.emit("All data cleared successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit("Error clearing data: ${e.localizedMessage}")
            }
        }
    }
}
