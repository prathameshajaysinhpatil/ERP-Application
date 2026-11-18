package com.example.balaji_earthmovers.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class DriverWithVehicle(
    @Embedded val driver: DriverEntity,
    @Relation(
        parentColumn = "assignedVehicleId",
        entityColumn = "id"
    )
    val vehicle: VehicleEntity?
)

data class VehicleWithDriver(
    @Embedded val vehicle: VehicleEntity,
    @Relation(
        parentColumn = "currentDriverId",
        entityColumn = "id"
    )
    val driver: DriverEntity?
)

data class WorkWithDetails(
    @Embedded val work: WorkEntity,
    @Relation(
        parentColumn = "vehicleId",
        entityColumn = "id"
    )
    val vehicle: VehicleEntity,
    @Relation(
        parentColumn = "driverId",
        entityColumn = "id"
    )
    val driver: DriverEntity,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: CustomerEntity
)

data class DriverWithTransactions(
    @Embedded val driver: DriverEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "driverId"
    )
    val transactions: List<DriverTransactionEntity>
)

data class CustomerWithHistory(
    @Embedded val customer: CustomerEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val works: List<WorkEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val incomes: List<IncomeEntity>
)

data class CustomerAgingSummary(
    val customer: CustomerEntity,
    val current0To30: Double,
    val overdue31To60: Double,
    val overdue61To90: Double,
    val overdue90Plus: Double,
    val totalPending: Double
)
