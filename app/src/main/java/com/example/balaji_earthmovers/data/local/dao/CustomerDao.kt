package com.example.balaji_earthmovers.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.balaji_earthmovers.data.local.entity.CustomerEntity
import com.example.balaji_earthmovers.data.local.entity.CustomerWithHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers WHERE deletedAt IS NULL ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id AND deletedAt IS NULL")
    fun getCustomerById(id: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE deletedAt IS NULL AND (totalBilled - totalPaid) > 0 ORDER BY (totalBilled - totalPaid) DESC")
    fun getCustomersWithPendingBalance(): Flow<List<CustomerEntity>>

    @Transaction
    @Query("SELECT * FROM customers WHERE id = :customerId AND deletedAt IS NULL")
    fun getCustomerWithHistory(customerId: String): Flow<CustomerWithHistory?>

    @Query("SELECT IFNULL(SUM(totalBilled - totalPaid), 0.0) FROM customers WHERE deletedAt IS NULL")
    fun getTotalPendingReceivables(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET deletedAt = :timestamp WHERE id = :customerId")
    suspend fun softDeleteCustomer(customerId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE customers SET totalBilled = totalBilled + :amount WHERE id = :customerId")
    suspend fun incrementTotalBilled(customerId: String, amount: Double)

    @Query("UPDATE customers SET totalPaid = totalPaid + :amount WHERE id = :customerId")
    suspend fun incrementTotalPaid(customerId: String, amount: Double)

    /**
     * Receivables Aging breakdown queries by unpaid income records
     */
    @Query("""
        SELECT IFNULL(SUM(pendingAmount), 0.0) FROM incomes 
        WHERE customerId = :customerId AND pendingAmount > 0 AND deletedAt IS NULL 
        AND date >= :start30DaysAgo
    """)
    fun getCustomerAging0To30(customerId: String, start30DaysAgo: Long): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(pendingAmount), 0.0) FROM incomes 
        WHERE customerId = :customerId AND pendingAmount > 0 AND deletedAt IS NULL 
        AND date BETWEEN :start60DaysAgo AND :end30DaysAgo
    """)
    fun getCustomerAging31To60(customerId: String, start60DaysAgo: Long, end30DaysAgo: Long): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(pendingAmount), 0.0) FROM incomes 
        WHERE customerId = :customerId AND pendingAmount > 0 AND deletedAt IS NULL 
        AND date BETWEEN :start90DaysAgo AND :end60DaysAgo
    """)
    fun getCustomerAging61To90(customerId: String, start90DaysAgo: Long, end60DaysAgo: Long): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(pendingAmount), 0.0) FROM incomes 
        WHERE customerId = :customerId AND pendingAmount > 0 AND deletedAt IS NULL 
        AND date < :start90DaysAgo
    """)
    fun getCustomerAging90Plus(customerId: String, start90DaysAgo: Long): Flow<Double>
}
