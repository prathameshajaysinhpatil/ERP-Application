package com.example.balaji_earthmovers.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = DriverEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkedDriverId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkedVehicleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("linkedDriverId"),
        Index("linkedVehicleId"),
        Index("expenseType"),
        Index("date")
    ]
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val expenseName: String,
    val expenseType: ExpenseType,
    val description: String = "",
    val amount: Double,
    val date: Long,
    val linkedDriverId: String? = null,
    val linkedVehicleId: String? = null,
    val deletedAt: Long? = null
)
