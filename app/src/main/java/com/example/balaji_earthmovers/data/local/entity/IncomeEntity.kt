package com.example.balaji_earthmovers.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "incomes",
    foreignKeys = [
        ForeignKey(
            entity = WorkEntity::class,
            parentColumns = ["id"],
            childColumns = ["workId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("workId"),
        Index("customerId"),
        Index("date")
    ]
)
data class IncomeEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val workId: String? = null,
    val customerId: String,
    val date: Long,
    val totalWorkAmount: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val paymentMode: PaymentMode,
    val transactionReferenceId: String = "",
    val deletedAt: Long? = null
)
