package com.example.balaji_earthmovers.data.repository

import com.example.balaji_earthmovers.data.local.AppDatabase
import com.example.balaji_earthmovers.data.local.entity.CustomerAgingSummary
import com.example.balaji_earthmovers.data.local.entity.CustomerEntity
import com.example.balaji_earthmovers.data.local.entity.CustomerWithHistory
import com.example.balaji_earthmovers.data.local.entity.DriverEntity
import com.example.balaji_earthmovers.data.local.entity.DriverTransactionEntity
import com.example.balaji_earthmovers.data.local.entity.DriverWithTransactions
import com.example.balaji_earthmovers.data.local.entity.ExpenseEntity
import com.example.balaji_earthmovers.data.local.entity.ExpenseType
import com.example.balaji_earthmovers.data.local.entity.IncomeEntity
import com.example.balaji_earthmovers.data.local.entity.PaymentMode
import com.example.balaji_earthmovers.data.local.entity.VehicleEntity
import com.example.balaji_earthmovers.data.local.entity.VehicleWithDriver
import com.example.balaji_earthmovers.data.local.entity.WorkEntity
import com.example.balaji_earthmovers.data.local.entity.WorkWithDetails
import com.example.balaji_earthmovers.data.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class BalajiRepository(private val database: AppDatabase) {

    private val driverDao = database.driverDao()
    private val vehicleDao = database.vehicleDao()
    private val customerDao = database.customerDao()
    private val workDao = database.workDao()
    private val expenseDao = database.expenseDao()
    private val incomeDao = database.incomeDao()

    // --- DRIVERS ---
    val allDrivers: Flow<List<DriverEntity>> = driverDao.getAllDrivers()

    fun getDriverWithTransactions(driverId: String): Flow<DriverWithTransactions?> =
        driverDao.getDriverWithTransactions(driverId)

    fun getTransactionsForDriver(driverId: String): Flow<List<DriverTransactionEntity>> =
        driverDao.getTransactionsForDriver(driverId)

    suspend fun insertDriver(driver: DriverEntity) {
        val encryptedAadhaar = if (driver.adharNumber.isNotBlank()) {
            CryptoManager.encrypt(driver.adharNumber)
        } else ""
        driverDao.insertDriver(driver.copy(adharNumber = encryptedAadhaar))
    }

    suspend fun updateDriver(driver: DriverEntity) {
        val encryptedAadhaar = if (driver.adharNumber.isNotBlank()) {
            if (driver.adharNumber.length == 12 && driver.adharNumber.all { it.isDigit() }) {
                CryptoManager.encrypt(driver.adharNumber)
            } else {
                driver.adharNumber
            }
        } else ""
        driverDao.updateDriver(driver.copy(adharNumber = encryptedAadhaar))
    }

    suspend fun addDriverPayout(transaction: DriverTransactionEntity) {
        driverDao.addDriverPayout(transaction)
    }

    suspend fun softDeleteDriver(driverId: String) {
        driverDao.softDeleteDriver(driverId)
    }

    // --- VEHICLES ---
    val allVehicles: Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()
    val allVehiclesWithDrivers: Flow<List<VehicleWithDriver>> = vehicleDao.getAllVehiclesWithDrivers()
    val vehiclesDueForService: Flow<List<VehicleEntity>> = vehicleDao.getVehiclesDueForService()

    fun getVehiclesActiveInSeason(startDate: Long, endDate: Long): Flow<List<VehicleEntity>> =
        vehicleDao.getVehiclesActiveBetweenDates(startDate, endDate)

    suspend fun insertVehicle(vehicle: VehicleEntity) {
        vehicleDao.insertVehicle(vehicle)
    }

    suspend fun updateVehicle(vehicle: VehicleEntity) {
        vehicleDao.updateVehicle(vehicle)
    }

    suspend fun markVehicleServiced(vehicleId: String) {
        vehicleDao.markVehicleServiced(vehicleId)
    }

    suspend fun softDeleteVehicle(vehicleId: String) {
        vehicleDao.softDeleteVehicle(vehicleId)
    }

    // --- CUSTOMERS & RECEIVABLES AGING ---
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val totalPendingReceivables: Flow<Double> = customerDao.getTotalPendingReceivables()

    fun getCustomerWithHistory(customerId: String): Flow<CustomerWithHistory?> =
        customerDao.getCustomerWithHistory(customerId)

    suspend fun insertCustomer(customer: CustomerEntity) {
        customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDao.updateCustomer(customer)
    }

    suspend fun softDeleteCustomer(customerId: String) {
        customerDao.softDeleteCustomer(customerId)
    }

    /**
     * Calculates Receivables Aging breakdown for a customer across:
     * 0-30 days, 31-60 days, 61-90 days, 90+ days.
     */
    fun getCustomerAgingSummary(customerId: String): Flow<CustomerAgingSummary?> {
        val now = System.currentTimeMillis()
        val d30 = now - (30L * 24 * 3600 * 1000)
        val d60 = now - (60L * 24 * 3600 * 1000)
        val d90 = now - (90L * 24 * 3600 * 1000)

        return combine(
            customerDao.getCustomerById(customerId),
            customerDao.getCustomerAging0To30(customerId, d30),
            customerDao.getCustomerAging31To60(customerId, d60, d30),
            customerDao.getCustomerAging61To90(customerId, d90, d60),
            customerDao.getCustomerAging90Plus(customerId, d90)
        ) { customer, a0to30, a31to60, a61to90, a90plus ->
            customer?.let {
                CustomerAgingSummary(
                    customer = it,
                    current0To30 = a0to30,
                    overdue31To60 = a31to60,
                    overdue61To90 = a61to90,
                    overdue90Plus = a90plus,
                    totalPending = it.pendingBalance
                )
            }
        }
    }

    /**
     * Aging breakdown across all customers with outstanding balances:
     * Accurately allocates the customer's active pendingBalance across 0-30, 31-60, 61-90, 90+ buckets
     * based on their chronological work logs, ensuring the bucket sum mathematically matches totalPending.
     */
    fun getAllCustomersAgingSummary(): Flow<List<CustomerAgingSummary>> {
        val now = System.currentTimeMillis()
        val d30 = now - (30L * 24 * 3600 * 1000)
        val d60 = now - (60L * 24 * 3600 * 1000)
        val d90 = now - (90L * 24 * 3600 * 1000)

        return combine(
            customerDao.getAllCustomers(),
            workDao.getAllWorks()
        ) { customers, works ->
            customers.map { customer ->
                val pending = customer.pendingBalance
                if (pending <= 0.0) {
                    CustomerAgingSummary(
                        customer = customer,
                        current0To30 = 0.0,
                        overdue31To60 = 0.0,
                        overdue61To90 = 0.0,
                        overdue90Plus = 0.0,
                        totalPending = 0.0
                    )
                } else {
                    val customerWorks = works.filter { it.customerId == customer.id }
                        .sortedByDescending { it.date }

                    var remainingDues = pending
                    var a0to30 = 0.0
                    var a31to60 = 0.0
                    var a61to90 = 0.0
                    var a90plus = 0.0

                    for (w in customerWorks) {
                        if (remainingDues <= 0.0) break
                        val allocated = minOf(w.totalWorkAmount, remainingDues)
                        remainingDues -= allocated

                        when {
                            w.date >= d30 -> a0to30 += allocated
                            w.date >= d60 -> a31to60 += allocated
                            w.date >= d90 -> a61to90 += allocated
                            else -> a90plus += allocated
                        }
                    }

                    if (remainingDues > 0.0) {
                        a0to30 += remainingDues
                    }

                    CustomerAgingSummary(
                        customer = customer,
                        current0To30 = a0to30,
                        overdue31To60 = a31to60,
                        overdue61To90 = a61to90,
                        overdue90Plus = a90plus,
                        totalPending = pending
                    )
                }
            }
        }
    }

    // --- WORKS & ATOMIC TRANSACTIONS ---
    val allWorksWithDetails: Flow<List<WorkWithDetails>> = workDao.getAllWorksWithDetails()
    val totalWorkBilled: Flow<Double> = workDao.getTotalWorkBilled()
    val totalOperatingHours: Flow<Double> = workDao.getTotalOperatingHours()

    suspend fun createWorkAtomic(
        work: WorkEntity,
        initialPaidAmount: Double = 0.0,
        paymentMode: PaymentMode = PaymentMode.CASH,
        referenceId: String = ""
    ) {
        workDao.createWorkEntryWithAtomicUpdates(
            work = work,
            initialPaidAmount = initialPaidAmount,
            paymentMode = paymentMode,
            referenceId = referenceId
        )
    }

    suspend fun updateWork(work: WorkEntity) {
        workDao.updateWork(work)
    }

    suspend fun softDeleteWork(workId: String) {
        workDao.deleteWorkAtomic(workId)
    }

    // --- EXPENSES & ATOMIC TRANSACTIONS ---
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val totalExpenses: Flow<Double> = expenseDao.getTotalExpenses()

    suspend fun recordExpenseAtomic(expense: ExpenseEntity, driverNotes: String = "") {
        expenseDao.recordExpenseWithAtomicUpdates(expense, driverNotes)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun softDeleteExpense(expenseId: String) {
        expenseDao.deleteExpenseAtomic(expenseId)
    }

    // --- INCOMES & PAYMENTS ---
    val allIncomes: Flow<List<IncomeEntity>> = incomeDao.getAllIncomes()
    val totalCollectedIncome: Flow<Double> = incomeDao.getTotalCollectedIncome()

    suspend fun recordPaymentReceipt(income: IncomeEntity) {
        incomeDao.recordPaymentReceipt(income)
    }

    suspend fun updateIncome(income: IncomeEntity) {
        incomeDao.updateIncome(income)
    }

    suspend fun settlePendingIncome(incomeId: String, amount: Double) {
        incomeDao.settlePendingIncome(incomeId, amount)
    }

    suspend fun softDeleteIncome(incomeId: String) {
        incomeDao.deleteIncomeAtomic(incomeId)
    }

    suspend fun clearAllData() {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            database.clearAllTables()
        }
    }
}
