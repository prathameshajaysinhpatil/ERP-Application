package com.example.balaji_earthmovers.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.balaji_earthmovers.data.local.entity.DriverTransactionEntity
import com.example.balaji_earthmovers.data.local.entity.DriverTransactionType
import com.example.balaji_earthmovers.data.local.entity.ExpenseEntity
import com.example.balaji_earthmovers.data.local.entity.ExpenseType
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ExpenseDao {

    @Query("SELECT * FROM expenses WHERE deletedAt IS NULL ORDER BY date DESC")
    abstract fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE expenseType = :type AND deletedAt IS NULL ORDER BY date DESC")
    abstract fun getExpensesByType(type: ExpenseType): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE deletedAt IS NULL AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    abstract fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT IFNULL(SUM(amount), 0.0) FROM expenses WHERE deletedAt IS NULL")
    abstract fun getTotalExpenses(): Flow<Double>

    @Query("SELECT IFNULL(SUM(amount), 0.0) FROM expenses WHERE deletedAt IS NULL AND date BETWEEN :startDate AND :endDate")
    abstract fun getTotalExpensesBetweenDates(startDate: Long, endDate: Long): Flow<Double>

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    abstract suspend fun getExpenseById(expenseId: String): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    abstract suspend fun updateExpense(expense: ExpenseEntity)

    @Query("UPDATE expenses SET deletedAt = :timestamp WHERE id = :expenseId")
    abstract suspend fun softDeleteExpense(expenseId: String, timestamp: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDriverTransaction(tx: DriverTransactionEntity)

    @Query("DELETE FROM driver_transactions WHERE expenseId = :expenseId")
    abstract suspend fun deleteDriverTransactionsForExpense(expenseId: String)

    @Query("UPDATE drivers SET givenSalary = givenSalary + :amount WHERE id = :driverId")
    abstract suspend fun incrementDriverGivenSalary(driverId: String, amount: Double)

    @Query("UPDATE drivers SET givenSalary = CASE WHEN (givenSalary - :amount) < 0 THEN 0.0 ELSE (givenSalary - :amount) END WHERE id = :driverId")
    abstract suspend fun decrementDriverGivenSalary(driverId: String, amount: Double)

    @Query("UPDATE vehicles SET fuelExpenseTotal = fuelExpenseTotal + :amount WHERE id = :vehicleId")
    abstract suspend fun incrementVehicleFuelExpense(vehicleId: String, amount: Double)

    @Query("UPDATE vehicles SET fuelExpenseTotal = CASE WHEN (fuelExpenseTotal - :amount) < 0 THEN 0.0 ELSE (fuelExpenseTotal - :amount) END WHERE id = :vehicleId")
    abstract suspend fun decrementVehicleFuelExpense(vehicleId: String, amount: Double)

    /**
     * Atomic Room @Transaction for multi-table updates:
     * - SALARY: auto-inserts DriverTransactionEntity + increments driver's givenSalary
     * - FUEL: auto-increments vehicle's fuelExpenseTotal
     */
    @Transaction
    open suspend fun recordExpenseWithAtomicUpdates(
        expense: ExpenseEntity,
        driverTransactionNotes: String = ""
    ) {
        insertExpense(expense)
        if (expense.expenseType == ExpenseType.SALARY && !expense.linkedDriverId.isNullOrBlank()) {
            val driverTx = DriverTransactionEntity(
                driverId = expense.linkedDriverId,
                expenseId = expense.id,
                amount = expense.amount,
                transactionType = DriverTransactionType.SALARY,
                date = expense.date,
                notes = if (driverTransactionNotes.isNotBlank()) driverTransactionNotes else expense.expenseName
            )
            insertDriverTransaction(driverTx)
            incrementDriverGivenSalary(expense.linkedDriverId, expense.amount)
        } else if (expense.expenseType == ExpenseType.FUEL && !expense.linkedVehicleId.isNullOrBlank()) {
            incrementVehicleFuelExpense(expense.linkedVehicleId, expense.amount)
        }
    }

    /**
     * Atomic deletion of expense:
     * Decreases total spend on dashboard and reverts driver given salary / vehicle fuel.
     */
    @Transaction
    open suspend fun deleteExpenseAtomic(expenseId: String, timestamp: Long = System.currentTimeMillis()) {
        val expense = getExpenseById(expenseId) ?: return
        softDeleteExpense(expenseId, timestamp)
        if (expense.expenseType == ExpenseType.SALARY && !expense.linkedDriverId.isNullOrBlank()) {
            decrementDriverGivenSalary(expense.linkedDriverId, expense.amount)
            deleteDriverTransactionsForExpense(expenseId)
        } else if (expense.expenseType == ExpenseType.FUEL && !expense.linkedVehicleId.isNullOrBlank()) {
            decrementVehicleFuelExpense(expense.linkedVehicleId, expense.amount)
        }
    }
}
