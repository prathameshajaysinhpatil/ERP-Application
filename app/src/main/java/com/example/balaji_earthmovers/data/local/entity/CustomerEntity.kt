package com.example.balaji_earthmovers.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "customers",
    indices = [
        Index("mobileNumber"),
        Index("name")
    ]
)
data class CustomerEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val mobileNumber: String,
    val address: String,
    val gstin: String = "",
    val totalBilled: Double = 0.0,
    val totalPaid: Double = 0.0,
    val imageUri: String? = null,
    val deletedAt: Long? = null
) {
    /**
     * Integrated Accounts Receivable Ledger balance:
     * pendingBalance = totalBilled - totalPaid
     */
    val pendingBalance: Double
        get() = (totalBilled - totalPaid).coerceAtLeast(0.0)
}
