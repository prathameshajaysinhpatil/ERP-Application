package com.example.balaji_earthmovers.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.balaji_earthmovers.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class IncomeDao {

    @Query("SELECT * FROM incomes WHERE deletedAt IS NULL ORDER BY date DESC")
    abstract fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE customerId = :customerId AND deletedAt IS NULL ORDER BY date DESC")
    abstract fun getIncomesForCustomer(customerId: String): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE deletedAt IS NULL AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    abstract fun getIncomesBetweenDates(startDate: Long, endDate: Long): Flow<List<IncomeEntity>>

    @Query("SELECT IFNULL(SUM(paidAmount), 0.0) FROM incomes WHERE deletedAt IS NULL")
    abstract fun getTotalCollectedIncome(): Flow<Double>

    @Query("SELECT IFNULL(SUM(totalWorkAmount), 0.0) FROM incomes WHERE deletedAt IS NULL")
    abstract fun getTotalBilledRevenue(): Flow<Double>

    @Query("SELECT IFNULL(SUM(pendingAmount), 0.0) FROM incomes WHERE deletedAt IS NULL")
    abstract fun getTotalPendingReceivables(): Flow<Double>

    @Query("SELECT * FROM incomes WHERE id = :incomeId")
    abstract suspend fun getIncomeById(incomeId: String): IncomeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertIncome(income: IncomeEntity)

    @Update
    abstract suspend fun updateIncome(income: IncomeEntity)

    @Query("UPDATE incomes SET deletedAt = :timestamp WHERE id = :incomeId")
    abstract suspend fun softDeleteIncome(incomeId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE customers SET totalPaid = totalPaid + :amount WHERE id = :customerId")
    abstract suspend fun incrementCustomerPaid(customerId: String, amount: Double)

    @Query("UPDATE customers SET totalPaid = CASE WHEN (totalPaid - :amount) < 0 THEN 0.0 ELSE (totalPaid - :amount) END WHERE id = :customerId")
    abstract suspend fun decrementCustomerPaid(customerId: String, amount: Double)

    /**
     * Records a standalone customer payment (or advance/lump-sum)
     * and atomically increments customer's totalPaid ledger.
     */
    @Transaction
    open suspend fun recordPaymentReceipt(income: IncomeEntity) {
        insertIncome(income)
        incrementCustomerPaid(income.customerId, income.paidAmount)
    }

    /**
     * Atomically deletes an income receipt:
     * Marks income deleted (removing it from dashboard totalCollectedIncome & net earnings)
     * and decrements customer's totalPaid ledger so client outstanding dues increase correctly.
     */
    @Transaction
    open suspend fun deleteIncomeAtomic(incomeId: String, timestamp: Long = System.currentTimeMillis()) {
        val income = getIncomeById(incomeId) ?: return
        softDeleteIncome(incomeId, timestamp)
        if (income.paidAmount > 0.0) {
            decrementCustomerPaid(income.customerId, income.paidAmount)
        }
    }

    /**
     * Settles an existing pending income entry and updates customer totalPaid.
     */
    @Transaction
    open suspend fun settlePendingIncome(incomeId: String, additionalPaid: Double) {
        incrementCustomerPaidForIncome(incomeId, additionalPaid)
        updatePendingIncomeAmounts(incomeId, additionalPaid)
    }

    @Query("UPDATE customers SET totalPaid = totalPaid + :amount WHERE id = (SELECT customerId FROM incomes WHERE id = :incomeId)")
    abstract suspend fun incrementCustomerPaidForIncome(incomeId: String, amount: Double)

    @Query("UPDATE incomes SET paidAmount = paidAmount + :amount, pendingAmount = CASE WHEN (pendingAmount - :amount) < 0 THEN 0.0 ELSE (pendingAmount - :amount) END WHERE id = :incomeId")
    abstract suspend fun updatePendingIncomeAmounts(incomeId: String, amount: Double)
}
