package com.example.balaji_earthmovers.data.local.backup

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.balaji_earthmovers.data.local.AppDatabase
import com.example.balaji_earthmovers.data.local.entity.CustomerEntity
import com.example.balaji_earthmovers.data.local.entity.DriverEntity
import com.example.balaji_earthmovers.data.local.entity.DriverTransactionEntity
import com.example.balaji_earthmovers.data.local.entity.ExpenseEntity
import com.example.balaji_earthmovers.data.local.entity.IncomeEntity
import com.example.balaji_earthmovers.data.local.entity.VehicleEntity
import com.example.balaji_earthmovers.data.local.entity.WorkEntity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ErpBackupPayload(
    val exportTimestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val drivers: List<DriverEntity> = emptyList(),
    val driverTransactions: List<DriverTransactionEntity> = emptyList(),
    val vehicles: List<VehicleEntity> = emptyList(),
    val works: List<WorkEntity> = emptyList(),
    val customers: List<CustomerEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val incomes: List<IncomeEntity> = emptyList()
)

object BackupRestoreManager {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * Resolves or creates the local storage directory named 'balajiearthmovers'.
     * Priority: Documents/balajiearthmovers -> ExternalStorage/balajiearthmovers -> App External/balajiearthmovers -> Files/balajiearthmovers.
     */
    fun getBalajiStorageDirectory(context: Context): File {
        // 1. Try Public Documents/balajiearthmovers
        try {
            val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val balajiDocs = File(docsDir, "balajiearthmovers")
            if (!balajiDocs.exists()) {
                balajiDocs.mkdirs()
            }
            if (balajiDocs.exists() && balajiDocs.canWrite()) {
                return balajiDocs
            }
        } catch (_: Exception) {}

        // 2. Try External Storage Root/balajiearthmovers
        try {
            val extRoot = Environment.getExternalStorageDirectory()
            val balajiExt = File(extRoot, "balajiearthmovers")
            if (!balajiExt.exists()) {
                balajiExt.mkdirs()
            }
            if (balajiExt.exists() && balajiExt.canWrite()) {
                return balajiExt
            }
        } catch (_: Exception) {}

        // 3. Try App External Files/balajiearthmovers
        try {
            val appExt = context.getExternalFilesDir(null)
            val balajiAppExt = File(appExt, "balajiearthmovers")
            if (!balajiAppExt.exists()) {
                balajiAppExt.mkdirs()
            }
            if (balajiAppExt.exists()) {
                return balajiAppExt
            }
        } catch (_: Exception) {}

        // 4. Fallback to App Internal Files/balajiearthmovers
        val internalDir = File(context.filesDir, "balajiearthmovers")
        if (!internalDir.exists()) {
            internalDir.mkdirs()
        }
        return internalDir
    }

    /**
     * Exports entire ERP database state to a JSON file inside the 'balajiearthmovers' folder.
     * Also publishes to MediaStore on Android Q+ to ensure immediate indexing in file explorers.
     */
    suspend fun exportJsonBackup(context: Context): Result<File> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            val payload = ErpBackupPayload(
                drivers = db.driverDao().getAllDrivers().first(),
                driverTransactions = db.driverDao().getAllDriverTransactions().first(),
                vehicles = db.vehicleDao().getAllVehicles().first(),
                works = db.workDao().getAllWorks().first(),
                customers = db.customerDao().getAllCustomers().first(),
                expenses = db.expenseDao().getAllExpenses().first(),
                incomes = db.incomeDao().getAllIncomes().first()
            )

            val targetDir = getBalajiStorageDirectory(context)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Balaji_Backup_$timeStamp.json"
            val backupFile = File(targetDir, fileName)

            val jsonBytes = gson.toJson(payload).toByteArray(Charsets.UTF_8)
            FileOutputStream(backupFile).use { fos ->
                fos.write(jsonBytes)
            }

            // On Android 10+ (API 29+), publish via MediaStore to Documents/balajiearthmovers
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/balajiearthmovers")
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { os ->
                            os.write(jsonBytes)
                        }
                    }
                } catch (msEx: Exception) {
                    msEx.printStackTrace()
                }
            }

            Result.success(backupFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Imports an ERP JSON backup from Uri or input stream and restores all records.
     */
    suspend fun importJsonBackup(context: Context, backupUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(backupUri)?.use { input ->
                input.bufferedReader().readText()
            } ?: return@withContext Result.failure(Exception("Unable to read backup file stream"))

            val payload = gson.fromJson(jsonString, ErpBackupPayload::class.java)
            val db = AppDatabase.getInstance(context)

            var totalRestored = 0
            payload.vehicles.forEach { db.vehicleDao().insertVehicle(it); totalRestored++ }
            payload.drivers.forEach { db.driverDao().insertDriver(it); totalRestored++ }
            payload.driverTransactions.forEach { db.driverDao().insertTransaction(it); totalRestored++ }
            payload.customers.forEach { db.customerDao().insertCustomer(it); totalRestored++ }
            payload.works.forEach { db.workDao().insertWork(it); totalRestored++ }
            payload.expenses.forEach { db.expenseDao().insertExpense(it); totalRestored++ }
            payload.incomes.forEach { db.incomeDao().insertIncome(it); totalRestored++ }

            Result.success(totalRestored)
        } catch (e: Exception) {
            // Attempt binary DB restore if file was SQLite .db
            try {
                val currentDb = context.getDatabasePath(AppDatabase.DATABASE_NAME)
                currentDb.parentFile?.mkdirs()
                context.contentResolver.openInputStream(backupUri)?.use { input ->
                    FileOutputStream(currentDb).use { output ->
                        input.copyTo(output)
                    }
                }
                Result.success(1)
            } catch (ex: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Performs a direct SQLite database file backup (.db file copy) into 'balajiearthmovers' folder.
     */
    suspend fun exportSqliteDbFile(context: Context): Result<File> = withContext(Dispatchers.IO) {
        try {
            val currentDb = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            if (!currentDb.exists()) {
                return@withContext Result.failure(Exception("Database file does not exist"))
            }

            val targetDir = getBalajiStorageDirectory(context)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(targetDir, "balaji_db_$timeStamp.db")

            FileInputStream(currentDb).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }
            Result.success(backupFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
