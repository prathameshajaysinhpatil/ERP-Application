package com.example.balaji_earthmovers.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "driver_transactions",
    foreignKeys = [
        ForeignKey(
            entity = DriverEntity::class,
            parentColumns = ["id"],
            childColumns = ["driverId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("driverId"),
        Index("expenseId"),
        Index("date")
    ]
)
data class DriverTransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val driverId: String,
    val expenseId: String? = null,
    val amount: Double,
    val transactionType: DriverTransactionType,
    val date: Long,
    val notes: String = "",
    val deletedAt: Long? = null
)
