package com.example.balaji_earthmovers.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for opening phone calls, SMS messages, and WhatsApp chats
 * with pre-formatted bilingual (English + Marathi) pending billing reminders.
 */
object CustomerShareUtils {

    fun generateBillingMessage(
        customerName: String,
        workName: String? = null,
        dateMillis: Long = System.currentTimeMillis(),
        totalAmount: Double,
        pendingAmount: Double
    ): String {
        val dateFormatted = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(dateMillis))
        val workText = if (!workName.isNullOrBlank()) workName else "General Contracting"
        val totalFormatted = "%.0f".format(totalAmount)
        val pendingFormatted = "%.0f".format(pendingAmount)

        return """
Balaji Earthmovers Kodoli
Work : $workText   date:$dateFormatted
Total amount : $totalFormatted/-
Pending amount : $pendingFormatted/-

Please pay your pending bill.
कृपया आपली थकीत रक्कम लवकरात लवकर जमा करावी.
- Balaji Earthmovers Kodoli (Ajaysinh Jaysing Patil)
""".trimIndent()
    }

    fun openWhatsApp(
        context: Context,
        mobileNumber: String,
        customerName: String,
        workName: String? = null,
        dateMillis: Long = System.currentTimeMillis(),
        totalAmount: Double,
        pendingAmount: Double
    ) {
        val message = generateBillingMessage(
            customerName = customerName,
            workName = workName,
            dateMillis = dateMillis,
            totalAmount = totalAmount,
            pendingAmount = pendingAmount
        )
        sendWhatsAppText(context, mobileNumber, message)
    }

    fun sendWhatsAppText(context: Context, mobileNumber: String, message: String) {
        val cleanNumber = mobileNumber.filter { it.isDigit() }
        val formattedNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
        try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to generic SEND intent targeted to WhatsApp
            try {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    putExtra("jid", "$formattedNumber@s.whatsapp.net")
                    `package` = "com.whatsapp"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(sendIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "WhatsApp is not available on this device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openSms(
        context: Context,
        mobileNumber: String,
        customerName: String,
        workName: String? = null,
        dateMillis: Long = System.currentTimeMillis(),
        totalAmount: Double,
        pendingAmount: Double
    ) {
        val message = generateBillingMessage(
            customerName = customerName,
            workName = workName,
            dateMillis = dateMillis,
            totalAmount = totalAmount,
            pendingAmount = pendingAmount
        )
        sendSmsText(context, mobileNumber, message)
    }

    fun sendSmsText(context: Context, mobileNumber: String, message: String) {
        val cleanNumber = mobileNumber.filter { it.isDigit() || it == '+' }
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$cleanNumber")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open SMS application", Toast.LENGTH_SHORT).show()
        }
    }

    fun makeCall(context: Context, mobileNumber: String) {
        try {
            val clean = mobileNumber.filter { it.isDigit() || it == '+' }
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$clean")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open phone dialer", Toast.LENGTH_SHORT).show()
        }
    }
}
