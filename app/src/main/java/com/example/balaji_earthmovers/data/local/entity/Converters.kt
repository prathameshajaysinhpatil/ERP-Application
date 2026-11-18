package com.example.balaji_earthmovers.data.local.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class DriverTransactionType {
    ADVANCE,
    SALARY,
    BONUS
}

enum class ExpenseType {
    FUEL,
    SALARY,
    MAINTENANCE,
    GIFTS,
    PURCHASE,
    LOSS,
    OTHER
}

enum class PaymentMode {
    CASH,
    UPI,
    BANK_TRANSFER,
    CHEQUE
}

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTransactionType(type: DriverTransactionType?): String? = type?.name

    @TypeConverter
    fun toTransactionType(value: String?): DriverTransactionType? =
        value?.let { enumValueOf<DriverTransactionType>(it) }

    @TypeConverter
    fun fromExpenseType(type: ExpenseType?): String? = type?.name

    @TypeConverter
    fun toExpenseType(value: String?): ExpenseType? =
        value?.let { enumValueOf<ExpenseType>(it) }

    @TypeConverter
    fun fromPaymentMode(mode: PaymentMode?): String? = mode?.name

    @TypeConverter
    fun toPaymentMode(value: String?): PaymentMode? =
        value?.let { enumValueOf<PaymentMode>(it) }

    @TypeConverter
    fun fromWorkingDaysMap(map: Map<String, Int>?): String {
        return if (map == null) "{}" else gson.toJson(map)
    }

    @TypeConverter
    fun toWorkingDaysMap(json: String?): Map<String, Int> {
        if (json.isNullOrBlank()) return emptyMap()
        val type = object : TypeToken<Map<String, Int>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
