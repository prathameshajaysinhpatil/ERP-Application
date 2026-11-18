package com.example.balaji_earthmovers.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.balaji_earthmovers.data.local.entity.IncomeEntity
import com.example.balaji_earthmovers.data.local.entity.PaymentMode
import com.example.balaji_earthmovers.data.local.entity.WorkEntity
import com.example.balaji_earthmovers.data.local.entity.WorkWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkDao {

    @Query("SELECT * FROM works WHERE deletedAt IS NULL ORDER BY date DESC")
    abstract fun getAllWorks(): Flow<List<WorkEntity>>

    @Transaction
    @Query("SELECT * FROM works WHERE deletedAt IS NULL ORDER BY date DESC")
    abstract fun getAllWorksWithDetails(): Flow<List<WorkWithDetails>>

    @Query("SELECT * FROM works WHERE vehicleId = :vehicleId AND deletedAt IS NULL ORDER BY date DESC")
    abstract fun getWorksForVehicle(vehicleId: String): Flow<List<WorkEntity>>

    @Query("SELECT * FROM works WHERE driverId = :driverId AND deletedAt IS NULL ORDER BY date DESC")
    abstract fun getWorksForDriver(driverId: String): Flow<List<WorkEntity>>

    @Query("SELECT * FROM works WHERE customerId = :customerId AND deletedAt IS NULL ORDER BY date DESC")
    abstract fun getWorksForCustomer(customerId: String): Flow<List<WorkEntity>>

    @Query("SELECT * FROM works WHERE deletedAt IS NULL AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    abstract fun getWorksBetweenDates(startDate: Long, endDate: Long): Flow<List<WorkEntity>>

    @Query("SELECT IFNULL(SUM(totalWorkAmount), 0.0) FROM works WHERE deletedAt IS NULL")
    abstract fun getTotalWorkBilled(): Flow<Double>

    @Query("SELECT IFNULL(SUM(totalTimeHours), 0.0) FROM works WHERE deletedAt IS NULL")
    abstract fun getTotalOperatingHours(): Flow<Double>

    @Query("SELECT * FROM works WHERE id = :workId")
    abstract suspend fun getWorkById(workId: String): WorkEntity?

    @Query("SELECT * FROM incomes WHERE workId = :workId AND deletedAt IS NULL")
    abstract suspend fun getIncomesForWork(workId: String): List<IncomeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWork(work: WorkEntity)

    @Update
    abstract suspend fun updateWork(work: WorkEntity)

    @Query("UPDATE works SET deletedAt = :timestamp WHERE id = :workId")
    abstract suspend fun softDeleteWork(workId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE vehicles SET workedHours = workedHours + :hours WHERE id = :vehicleId")
    abstract suspend fun incrementVehicleWorkedHours(vehicleId: String, hours: Double)

    @Query("UPDATE vehicles SET workedHours = CASE WHEN (workedHours - :hours) < 0 THEN 0.0 ELSE (workedHours - :hours) END WHERE id = :vehicleId")
    abstract suspend fun decrementVehicleWorkedHours(vehicleId: String, hours: Double)

    @Query("UPDATE customers SET totalBilled = totalBilled + :amount WHERE id = :customerId")
    abstract suspend fun incrementCustomerBilled(customerId: String, amount: Double)

    @Query("UPDATE customers SET totalBilled = CASE WHEN (totalBilled - :amount) < 0 THEN 0.0 ELSE (totalBilled - :amount) END WHERE id = :customerId")
    abstract suspend fun decrementCustomerBilled(customerId: String, amount: Double)

    @Query("UPDATE customers SET totalPaid = totalPaid + :amount WHERE id = :customerId")
    abstract suspend fun incrementCustomerPaid(customerId: String, amount: Double)

    @Query("UPDATE customers SET totalPaid = CASE WHEN (totalPaid - :amount) < 0 THEN 0.0 ELSE (totalPaid - :amount) END WHERE id = :customerId")
    abstract suspend fun decrementCustomerPaid(customerId: String, amount: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertIncome(income: IncomeEntity)

    @Query("UPDATE incomes SET deletedAt = :timestamp WHERE workId = :workId AND deletedAt IS NULL")
    abstract suspend fun softDeleteIncomesForWork(workId: String, timestamp: Long)

    /**
     * Atomic Database Transaction for Work Creation:
     * a. Increments assigned vehicle's workedHours by totalTimeHours
     * b. Updates customer's total billed
     * c. If initial payment received > 0, increments customer's totalPaid and creates an income record
     *    which immediately factors into the dashboard's total revenue & net profit!
     */
    @Transaction
    open suspend fun createWorkEntryWithAtomicUpdates(
        work: WorkEntity,
        initialPaidAmount: Double = 0.0,
        paymentMode: PaymentMode = PaymentMode.CASH,
        referenceId: String = ""
    ) {
        insertWork(work)
        if (work.totalTimeHours > 0) {
            incrementVehicleWorkedHours(work.vehicleId, work.totalTimeHours)
        }
        incrementCustomerBilled(work.customerId, work.totalWorkAmount)

        if (initialPaidAmount > 0.0) {
            incrementCustomerPaid(work.customerId, initialPaidAmount)
            val income = IncomeEntity(
                workId = work.id,
                customerId = work.customerId,
                date = work.date,
                totalWorkAmount = work.totalWorkAmount,
                paidAmount = initialPaidAmount,
                pendingAmount = (work.totalWorkAmount - initialPaidAmount).coerceAtLeast(0.0),
                paymentMode = paymentMode,
                transactionReferenceId = referenceId
            )
            insertIncome(income)
        }
    }

    /**
     * Atomic Database Transaction for Work Deletion:
     * Reverts all atomic changes made during work creation:
     * a. Soft deletes work
     * b. Decrements vehicle hours
     * c. Decrements customer total billed
     * d. Soft deletes associated incomes and decrements customer total paid
     *    which immediately reduces dashboard net earnings!
     */
    @Transaction
    open suspend fun deleteWorkAtomic(workId: String, timestamp: Long = System.currentTimeMillis()) {
        val work = getWorkById(workId) ?: return
        softDeleteWork(workId, timestamp)
        if (work.totalTimeHours > 0) {
            decrementVehicleWorkedHours(work.vehicleId, work.totalTimeHours)
        }
        decrementCustomerBilled(work.customerId, work.totalWorkAmount)

        val linkedIncomes = getIncomesForWork(workId)
        val totalPaidOnWork = linkedIncomes.sumOf { it.paidAmount }
        if (totalPaidOnWork > 0.0) {
            decrementCustomerPaid(work.customerId, totalPaidOnWork)
        }
        softDeleteIncomesForWork(workId, timestamp)
    }
}
