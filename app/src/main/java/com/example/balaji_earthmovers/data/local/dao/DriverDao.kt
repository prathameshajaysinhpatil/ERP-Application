package com.example.balaji_earthmovers.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.balaji_earthmovers.data.local.entity.DriverEntity
import com.example.balaji_earthmovers.data.local.entity.DriverTransactionEntity
import com.example.balaji_earthmovers.data.local.entity.DriverWithTransactions
import com.example.balaji_earthmovers.data.local.entity.DriverWithVehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverDao {

    @Query("SELECT * FROM drivers WHERE deletedAt IS NULL ORDER BY driverName ASC")
    fun getAllDrivers(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers WHERE id = :id AND deletedAt IS NULL")
    fun getDriverById(id: String): Flow<DriverEntity?>

    @Transaction
    @Query("SELECT * FROM drivers WHERE deletedAt IS NULL ORDER BY driverName ASC")
    fun getAllDriversWithVehicles(): Flow<List<DriverWithVehicle>>

    @Transaction
    @Query("SELECT * FROM drivers WHERE id = :driverId AND deletedAt IS NULL")
    fun getDriverWithTransactions(driverId: String): Flow<DriverWithTransactions?>

    @Query("SELECT * FROM driver_transactions WHERE driverId = :driverId AND deletedAt IS NULL ORDER BY date DESC")
    fun getTransactionsForDriver(driverId: String): Flow<List<DriverTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverEntity)

    @Update
    suspend fun updateDriver(driver: DriverEntity)

    @Query("UPDATE drivers SET deletedAt = :timestamp WHERE id = :driverId")
    suspend fun softDeleteDriver(driverId: String, timestamp: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: DriverTransactionEntity)

    @Query("UPDATE drivers SET givenSalary = givenSalary + :amount WHERE id = :driverId")
    suspend fun incrementGivenSalary(driverId: String, amount: Double)

    @Query("UPDATE drivers SET assignedVehicleId = :vehicleId WHERE id = :driverId")
    suspend fun updateAssignedVehicle(driverId: String, vehicleId: String?)

    @Transaction
    suspend fun addDriverPayout(transaction: DriverTransactionEntity) {
        insertTransaction(transaction)
        incrementGivenSalary(transaction.driverId, transaction.amount)
    }
}
