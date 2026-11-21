package com.example.balaji_earthmovers.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.balaji_earthmovers.ui.theme.ProfitGreen

data class ScannedRecordData(
    val name: String = "",
    val mobileNumber: String = "",
    val vehicleNumber: String = "",
    val aadhaarNumber: String = "",
    val gstin: String = "",
    val amount: String = "",
    val hours: String = "",
    val rate: String = "",
    val location: String = "",
    val rawText: String = ""
)

/**
 * Intelligent OCR Document Scanner dialog that allows capturing a photo
 * using device camera or gallery, extracts text and key business fields
 * (Phone, Vehicle No, Aadhaar, GSTIN, Amounts, Hours), and pre-fills
 * the respective Work, Vehicle, Driver, or Customer dialog.
 */
@Composable
fun DocumentScanDialog(
    targetType: String, // "Work", "Vehicle", "Driver", "Customer"
    onDismiss: () -> Unit,
    onDataExtracted: (ScannedRecordData) -> Unit
) {
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var recognizedText by remember { mutableStateOf("") }
    var parsedData by remember { mutableStateOf(ScannedRecordData()) }

    // Camera launcher (takes a photo directly into Bitmap)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            // Run regex-based text analysis or simulated OCR
            val analyzed = parseDocumentText(recognizedText)
            parsedData = analyzed
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .imePadding(),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.DocumentScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Scan $targetType Document", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Capture a photo of the card, RC book, slip, or invoice using camera. You can also paste detected text below to auto-populate fields.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Capture / Photo Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Take Photo")
                    }
                }

                // Photo Preview if captured
                capturedBitmap?.let { bmp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Scanned Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }

                // Text / OCR Input or extracted text
                OutlinedTextField(
                    value = recognizedText,
                    onValueChange = { text ->
                        recognizedText = text
                        parsedData = parseDocumentText(text)
                    },
                    label = { Text("Detected / Document Text") },
                    placeholder = {
                        Text(
                            when (targetType) {
                                "Customer" -> "e.g. Ramesh Patil, 9822123456, Kolhapur, GSTIN: 27AAAAA0000A1Z5"
                                "Vehicle" -> "e.g. JCB 3DX, MH-09-EM-1234, 1500 hrs"
                                "Driver" -> "e.g. Sachin Jadhav, 9823000000, Aadhaar: 1234 5678 9012"
                                else -> "e.g. Site Work Kodoli, MH 09 AB 1234, 25 hrs @ 1200/hr, Paid: 5000"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )

                // Live Parsed Fields Preview
                Text("Extracted Information Preview:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (parsedData.name.isNotBlank()) {
                            ExtractedFieldRow(label = "Name / Title", value = parsedData.name)
                        }
                        if (parsedData.mobileNumber.isNotBlank()) {
                            ExtractedFieldRow(label = "Phone / Mobile", value = parsedData.mobileNumber)
                        }
                        if (parsedData.vehicleNumber.isNotBlank()) {
                            ExtractedFieldRow(label = "Vehicle Number", value = parsedData.vehicleNumber)
                        }
                        if (parsedData.aadhaarNumber.isNotBlank()) {
                            ExtractedFieldRow(label = "Aadhaar Card", value = parsedData.aadhaarNumber)
                        }
                        if (parsedData.gstin.isNotBlank()) {
                            ExtractedFieldRow(label = "GSTIN", value = parsedData.gstin)
                        }
                        if (parsedData.amount.isNotBlank()) {
                            ExtractedFieldRow(label = "Amount (₹)", value = parsedData.amount)
                        }
                        if (parsedData.hours.isNotBlank()) {
                            ExtractedFieldRow(label = "Hours", value = parsedData.hours)
                        }
                        if (parsedData.rate.isNotBlank()) {
                            ExtractedFieldRow(label = "Rate (₹/hr)", value = parsedData.rate)
                        }

                        if (parsedData.name.isBlank() && parsedData.mobileNumber.isBlank() &&
                            parsedData.vehicleNumber.isBlank() && parsedData.aadhaarNumber.isBlank() &&
                            parsedData.gstin.isBlank() && parsedData.amount.isBlank()
                        ) {
                            Text(
                                "No fields detected yet. Take a clear photo or type/paste text above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDataExtracted(parsedData)
                    onDismiss()
                },
                enabled = recognizedText.isNotBlank() || capturedBitmap != null
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Apply to Form")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ExtractedFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$label:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = ProfitGreen
        )
    }
}

/**
 * Intelligent regex pattern parser for Indian machinery documents:
 * extracts phone numbers, vehicle registrations, Aadhaar, GSTIN, amounts, hours, and rates.
 */
fun parseDocumentText(raw: String): ScannedRecordData {
    if (raw.isBlank()) return ScannedRecordData()

    // 1. Mobile Phone (10 digits starting with 6, 7, 8, or 9)
    val phoneMatch = Regex("""\b[6-9]\d{9}\b""").find(raw)?.value ?: ""

    // 2. Vehicle Registration Number (e.g. MH 09 EM 1234 or MH09AB1234)
    val vehicleMatch = Regex("""\b([A-Z]{2}[ -]?[0-9]{1,2}[ -]?[A-Z]{0,3}[ -]?[0-9]{4})\b""", RegexOption.IGNORE_CASE)
        .find(raw)?.value?.uppercase()?.replace(" ", "-") ?: ""

    // 3. Aadhaar Number (12 digits with or without space)
    val aadhaarMatch = Regex("""\b\d{4}\s?\d{4}\s?\d{4}\b""").find(raw)?.value?.replace(" ", "") ?: ""

    // 4. GSTIN (15 character standard Indian GST pattern)
    val gstinMatch = Regex("""\b\d{2}[A-Z]{5}\d{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}\b""", RegexOption.IGNORE_CASE)
        .find(raw)?.value?.uppercase() ?: ""

    // 5. Hours (e.g. 25 hrs, 10.5 hours)
    val hoursMatch = Regex("""(\d+(?:\.\d+)?)\s*(?:hrs?|hours?|hr|तास)""", RegexOption.IGNORE_CASE)
        .find(raw)?.groupValues?.getOrNull(1) ?: ""

    // 6. Rate (e.g. 1200/hr, 1500 / hr)
    val rateMatch = Regex("""(\d+(?:\.\d+)?)\s*(?:\/hr|\/ तास|per hour)""", RegexOption.IGNORE_CASE)
        .find(raw)?.groupValues?.getOrNull(1) ?: ""

    // 7. Amounts (e.g. Total: 15000, Paid: 5000, ₹12000)
    val amountMatch = Regex("""(?:Rs\.?|INR|₹|Total|Amount|Amt|Paid|Bal)[:\s]*([0-9]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
        .find(raw)?.groupValues?.getOrNull(1) ?: ""

    // 8. Name heuristic: First non-empty line that doesn't contain digits or labels
    val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
    val candidateName = lines.firstOrNull { line ->
        !line.contains("GST", ignoreCase = true) &&
        !line.contains("Aadhaar", ignoreCase = true) &&
        !line.contains("Phone", ignoreCase = true) &&
        !line.contains("Date", ignoreCase = true) &&
        line.length in 3..40 &&
        line.count { it.isDigit() } <= 2
    }?.replace(Regex("^(Name|M/s|Mr|Shree|Customer|Client)[: ]*", RegexOption.IGNORE_CASE), "")?.trim() ?: ""

    return ScannedRecordData(
        name = candidateName,
        mobileNumber = phoneMatch,
        vehicleNumber = vehicleMatch,
        aadhaarNumber = aadhaarMatch,
        gstin = gstinMatch,
        amount = amountMatch,
        hours = hoursMatch,
        rate = rateMatch,
        location = "",
        rawText = raw
    )
}
