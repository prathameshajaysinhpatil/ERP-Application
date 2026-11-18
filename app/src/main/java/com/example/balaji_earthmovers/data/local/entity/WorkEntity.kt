package com.example.balaji_earthmovers.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "works",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = DriverEntity::class,
            parentColumns = ["id"],
            childColumns = ["driverId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("vehicleId"),
        Index("driverId"),
        Index("customerId"),
        Index("date")
    ]
)
data class WorkEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val workName: String,
    val vehicleId: String,
    val driverId: String,
    val customerId: String,
    val date: Long,
    val startTime: Long,
    val endTime: Long,
    val totalTimeHours: Double,
    val hourlyRate: Double,
    val totalWorkAmount: Double,
    val location: String = "",
    val deletedAt: Long? = null
)
