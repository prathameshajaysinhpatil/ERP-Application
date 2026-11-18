package com.example.balaji_earthmovers.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.balaji_earthmovers.data.local.dao.CustomerDao
import com.example.balaji_earthmovers.data.local.dao.DriverDao
import com.example.balaji_earthmovers.data.local.dao.ExpenseDao
import com.example.balaji_earthmovers.data.local.dao.IncomeDao
import com.example.balaji_earthmovers.data.local.dao.VehicleDao
import com.example.balaji_earthmovers.data.local.dao.WorkDao
import com.example.balaji_earthmovers.data.local.entity.Converters
import com.example.balaji_earthmovers.data.local.entity.CustomerEntity
import com.example.balaji_earthmovers.data.local.entity.DriverEntity
import com.example.balaji_earthmovers.data.local.entity.DriverTransactionEntity
import com.example.balaji_earthmovers.data.local.entity.DriverTransactionType
import com.example.balaji_earthmovers.data.local.entity.ExpenseEntity
import com.example.balaji_earthmovers.data.local.entity.ExpenseType
import com.example.balaji_earthmovers.data.local.entity.IncomeEntity
import com.example.balaji_earthmovers.data.local.entity.PaymentMode
import com.example.balaji_earthmovers.data.local.entity.VehicleEntity
import com.example.balaji_earthmovers.data.local.entity.WorkEntity
import com.example.balaji_earthmovers.data.security.CryptoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Database(
    entities = [
        DriverEntity::class,
        DriverTransactionEntity::class,
        VehicleEntity::class,
        WorkEntity::class,
        CustomerEntity::class,
        ExpenseEntity::class,
        IncomeEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun driverDao(): DriverDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun workDao(): WorkDao
    abstract fun customerDao(): CustomerDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao

    companion object {
        const val DATABASE_NAME = "balaji_earthmovers.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            // Strictly stored in internal storage: context.getDatabasePath(DATABASE_NAME)
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            dbFile.parentFile?.mkdirs()

            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial demo data asynchronously for first run
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(getInstance(context))
                        }
                    }
                })
                .build()
        }

        private suspend fun seedInitialData(database: AppDatabase) {
            val now = System.currentTimeMillis()
            val thirtyDaysAgo = now - (30L * 24 * 3600 * 1000)
            val sixtyDaysAgo = now - (60L * 24 * 3600 * 1000)

            // Seed Vehicles
            val v1 = VehicleEntity(
                id = "veh-jcb-3dx-1",
                vehicleName = "JCB 3DX Super",
                vehicleNumber = "MH-12-AB-1234",
                model = "2023 Heavy Duty",
                workedHours = 480.0,
                lastServiceHours = 220.0, // 260 hours -> Overdue for 250hr service warning!
                purchaseDate = sixtyDaysAgo,
                fuelExpenseTotal = 32000.0
            )
            val v2 = VehicleEntity(
                id = "veh-hitachi-ex200-2",
                vehicleName = "Tata Hitachi EX 200",
                vehicleNumber = "MH-12-CD-5678",
                model = "2022 Hydraulic Excavator",
                workedHours = 310.0,
                lastServiceHours = 200.0, // 110 hrs -> Healthy
                purchaseDate = sixtyDaysAgo,
                fuelExpenseTotal = 45000.0
            )
            database.vehicleDao().insertVehicle(v1)
            database.vehicleDao().insertVehicle(v2)

            // Seed Drivers
            val d1 = DriverEntity(
                id = "drv-ramesh-1",
                driverName = "Ramesh Patil",
                mobileNumber = "9823012345",
                address = "MIDC Road, Pune",
                adharNumber = CryptoManager.encrypt("123456789012"),
                monthlySalary = 25000.0,
                givenSalary = 15000.0,
                joiningDate = sixtyDaysAgo,
                assignedVehicleId = v1.id,
                totalLeaves = 2
            )
            val d2 = DriverEntity(
                id = "drv-suresh-2",
                driverName = "Suresh Shinde",
                mobileNumber = "9823098765",
                address = "Wakad, Pune",
                adharNumber = CryptoManager.encrypt("987654321098"),
                monthlySalary = 22000.0,
                givenSalary = 22000.0,
                joiningDate = thirtyDaysAgo,
                assignedVehicleId = v2.id,
                totalLeaves = 0
            )
            database.driverDao().insertDriver(d1)
            database.driverDao().insertDriver(d2)

            // Update assigned driver on vehicles
            database.vehicleDao().updateAssignedDriver(v1.id, d1.id)
            database.vehicleDao().updateAssignedDriver(v2.id, d2.id)

            // Seed Customers
            val c1 = CustomerEntity(
                id = "cust-shree-infra-1",
                name = "Shree Balaji Developers",
                mobileNumber = "9850112233",
                address = "Baner Road, Pune",
                gstin = "27AABCS1429B1Z8",
                totalBilled = 125000.0,
                totalPaid = 75000.0
            )
            val c2 = CustomerEntity(
                id = "cust-kisan-farm-2",
                name = "Kisanrao Jadhav (Farm Levelling)",
                mobileNumber = "9860223344",
                address = "Saswad, Pune",
                gstin = "",
                totalBilled = 48000.0,
                totalPaid = 48000.0
            )
            database.customerDao().insertCustomer(c1)
            database.customerDao().insertCustomer(c2)

            // Seed Driver Transactions (Passbook)
            val dt1 = DriverTransactionEntity(
                id = "dtx-1",
                driverId = d1.id,
                amount = 10000.0,
                transactionType = DriverTransactionType.SALARY,
                date = now - (15L * 24 * 3600 * 1000),
                notes = "Monthly Salary Transfer (UPI)"
            )
            val dt2 = DriverTransactionEntity(
                id = "dtx-2",
                driverId = d1.id,
                amount = 5000.0,
                transactionType = DriverTransactionType.ADVANCE,
                date = now - (3L * 24 * 3600 * 1000),
                notes = "Advance for Festival"
            )
            database.driverDao().insertTransaction(dt1)
            database.driverDao().insertTransaction(dt2)

            // Seed Works
            val w1 = WorkEntity(
                id = "work-1",
                workName = "Basement Excavation Plot 4B",
                vehicleId = v1.id,
                driverId = d1.id,
                customerId = c1.id,
                date = now - (10L * 24 * 3600 * 1000),
                startTime = now - (10L * 24 * 3600 * 1000),
                endTime = now - (10L * 24 * 3600 * 1000) + (8L * 3600 * 1000),
                totalTimeHours = 8.0,
                hourlyRate = 1800.0,
                totalWorkAmount = 14400.0,
                location = "Baner Highway Project"
            )
            database.workDao().insertWork(w1)

            // Seed Incomes
            val inc1 = IncomeEntity(
                id = "inc-1",
                workId = w1.id,
                customerId = c1.id,
                date = now - (10L * 24 * 3600 * 1000),
                totalWorkAmount = 14400.0,
                paidAmount = 10000.0,
                pendingAmount = 4400.0,
                paymentMode = PaymentMode.UPI,
                transactionReferenceId = "UPI/412398412891"
            )
            database.incomeDao().insertIncome(inc1)

            // Seed Expenses
            val exp1 = ExpenseEntity(
                id = "exp-1",
                expenseName = "Diesel 120 Litres",
                expenseType = ExpenseType.FUEL,
                description = "IOCL Pump Baner",
                amount = 11400.0,
                date = now - (7L * 24 * 3600 * 1000),
                linkedVehicleId = v1.id
            )
            database.expenseDao().insertExpense(exp1)
        }
    }
}
