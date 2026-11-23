package com.example.balaji_earthmovers.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "vehicles",
    foreignKeys = [
        ForeignKey(
            entity = DriverEntity::class,
            parentColumns = ["id"],
            childColumns = ["currentDriverId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("currentDriverId"),
        Index("vehicleNumber")
    ]
)
data class VehicleEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val vehicleName: String,
    val vehicleNumber: String,
    val model: String,
    val workedHours: Double = 0.0,
    val lastServiceHours: Double = 0.0,
    val purchaseDate: Long,
    val soldDate: Long? = null,
    val currentDriverId: String? = null,
    val fuelExpenseTotal: Double = 0.0,
    val imageUri: String? = null,
    val deletedAt: Long? = null
) {
    /**
     * Hours accumulated since last service.
     */
    val hoursSinceLastService: Double
        get() = (workedHours - lastServiceHours).coerceAtLeast(0.0)

    /**
     * Domain logic: Triggers a high-priority UI banner/badge when workedHours - lastServiceHours >= 250.0
     */
    val isServiceDue: Boolean
        get() = hoursSinceLastService >= 250.0

    val remainingHoursBeforeService: Double
        get() = (250.0 - hoursSinceLastService).coerceAtLeast(0.0)
}
