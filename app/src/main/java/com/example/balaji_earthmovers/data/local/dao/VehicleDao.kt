package com.example.balaji_earthmovers.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.balaji_earthmovers.data.local.entity.VehicleEntity
import com.example.balaji_earthmovers.data.local.entity.VehicleWithDriver
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Query("SELECT * FROM vehicles WHERE deletedAt IS NULL ORDER BY vehicleName ASC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id AND deletedAt IS NULL")
    fun getVehicleById(id: String): Flow<VehicleEntity?>

    @Transaction
    @Query("SELECT * FROM vehicles WHERE deletedAt IS NULL ORDER BY vehicleName ASC")
    fun getAllVehiclesWithDrivers(): Flow<List<VehicleWithDriver>>

    /**
     * High-priority service interval query:
     * Triggers banner/badge when workedHours - lastServiceHours >= 250.0
     */
    @Query("SELECT * FROM vehicles WHERE deletedAt IS NULL AND (workedHours - lastServiceHours) >= 250.0 ORDER BY (workedHours - lastServiceHours) DESC")
    fun getVehiclesDueForService(): Flow<List<VehicleEntity>>

    /**
     * Seasonal & date-range filtering for active fleet in specified intervals
     * (e.g., Monsoon, Peak Construction, or Monthly windows).
     */
    @Query("""
        SELECT DISTINCT v.* FROM vehicles v 
        INNER JOIN works w ON v.id = w.vehicleId 
        WHERE v.deletedAt IS NULL AND w.deletedAt IS NULL 
        AND w.date BETWEEN :startDate AND :endDate 
        ORDER BY v.vehicleName ASC
    """)
    fun getVehiclesActiveBetweenDates(startDate: Long, endDate: Long): Flow<List<VehicleEntity>>

    @Query("""
        SELECT IFNULL(SUM(e.amount), 0.0) FROM expenses e 
        WHERE e.linkedVehicleId = :vehicleId AND e.expenseType = 'FUEL' 
        AND e.deletedAt IS NULL AND e.date BETWEEN :startDate AND :endDate
    """)
    fun getVehicleFuelExpenseBetweenDates(vehicleId: String, startDate: Long, endDate: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET deletedAt = :timestamp WHERE id = :vehicleId")
    suspend fun softDeleteVehicle(vehicleId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE vehicles SET workedHours = workedHours + :hours WHERE id = :vehicleId")
    suspend fun incrementWorkedHours(vehicleId: String, hours: Double)

    @Query("UPDATE vehicles SET lastServiceHours = workedHours WHERE id = :vehicleId")
    suspend fun markVehicleServiced(vehicleId: String)

    @Query("UPDATE vehicles SET fuelExpenseTotal = fuelExpenseTotal + :amount WHERE id = :vehicleId")
    suspend fun incrementFuelExpense(vehicleId: String, amount: Double)

    @Query("UPDATE vehicles SET currentDriverId = :driverId WHERE id = :vehicleId")
    suspend fun updateAssignedDriver(vehicleId: String, driverId: String?)
}
