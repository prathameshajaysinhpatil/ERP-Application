package com.example.balaji_earthmovers.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.balaji_earthmovers.ui.theme.ProfitGreen
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

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
 * using device camera or gallery, extracts text and key business fields,
 * and allows the user to interactively select which text snippet goes where.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DocumentScanDialog(
    targetType: String, // "Work", "Vehicle", "Driver", "Customer"
    onDismiss: () -> Unit,
    onDataExtracted: (ScannedRecordData) -> Unit
) {
    val context = LocalContext.current
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var recognizedText by remember { mutableStateOf("") }
    var parsedData by remember { mutableStateOf(ScannedRecordData()) }
    var isScanning by remember { mutableStateOf(false) }

    fun processBitmapWithOcr(bitmap: Bitmap) {
        capturedBitmap = bitmap
        isScanning = true
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    isScanning = false
                    val text = visionText.text
                    recognizedText = text
                    val parsed = parseDocumentText(text)
                    parsedData = parsed
                    // Auto-fill directly and dismiss if parsed data has meaningful content
                    if (parsed.name.isNotBlank() || parsed.mobileNumber.isNotBlank() || parsed.vehicleNumber.isNotBlank() || parsed.aadhaarNumber.isNotBlank() || parsed.hours.isNotBlank() || parsed.amount.isNotBlank()) {
                        Toast.makeText(context, "Scanned & fields auto-filled! / माहिती आपोआप भरली गेली!", Toast.LENGTH_SHORT).show()
                        onDataExtracted(parsed)
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Text scanned! Please verify or map fields below.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    isScanning = false
                    Toast.makeText(context, "OCR scanning error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            isScanning = false
            Toast.makeText(context, "OCR setup error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera launcher (takes a photo directly into Bitmap)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            processBitmapWithOcr(bitmap)
        }
    }

    // Permission launcher for camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to launch camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to capture documents.", Toast.LENGTH_LONG).show()
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bmp = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
                processBitmapWithOcr(bmp)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchCameraSafe() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                Toast.makeText(context, "Camera error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Extract lines or tokens for manual field mapping
    val textChunks = remember(recognizedText) {
        recognizedText.split("\n", ",", ";", "|")
            .map { it.trim() }
            .filter { it.length >= 2 }
            .distinct()
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
                    "Capture a photo using camera or choose from gallery. Then paste/type the slip text or select which snippet goes into which field.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Capture / Gallery Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { launchCameraSafe() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Take Photo")
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                galleryLauncher.launch("image/*")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gallery error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("From Gallery")
                    }
                }

                if (isScanning) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Scanning document & auto-filling fields...",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Photo Preview if captured
                capturedBitmap?.let { bmp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
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
                    label = { Text("Document / Bill / Slip Text") },
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
                    maxLines = 5
                )

                // ==================== INTERACTIVE TEXT MAPPING ====================
                // Lets the user tap detected text snippets and choose which field they belong to
                if (textChunks.isNotEmpty()) {
                    Text(
                        "Map Detected Text Snippets (कोणते मजकूर कुठे ठेवायचे):",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            textChunks.take(8).forEach { snippet ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            "\"$snippet\"",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            AssistChip(
                                                onClick = { parsedData = parsedData.copy(name = snippet) },
                                                label = { Text("Name", fontSize = 10.sp) }
                                            )
                                            AssistChip(
                                                onClick = {
                                                    val cleanPhone = snippet.filter { it.isDigit() }
                                                    parsedData = parsedData.copy(mobileNumber = cleanPhone)
                                                },
                                                label = { Text("Mobile", fontSize = 10.sp) }
                                            )
                                            if (targetType == "Vehicle" || targetType == "Work") {
                                                AssistChip(
                                                    onClick = { parsedData = parsedData.copy(vehicleNumber = snippet.uppercase()) },
                                                    label = { Text("Vehicle No", fontSize = 10.sp) }
                                                )
                                            }
                                            if (targetType == "Driver") {
                                                AssistChip(
                                                    onClick = { parsedData = parsedData.copy(aadhaarNumber = snippet.filter { it.isDigit() }) },
                                                    label = { Text("Aadhaar", fontSize = 10.sp) }
                                                )
                                            }
                                            if (targetType == "Work") {
                                                AssistChip(
                                                    onClick = {
                                                        val amt = snippet.filter { it.isDigit() || it == '.' }
                                                        parsedData = parsedData.copy(amount = amt)
                                                    },
                                                    label = { Text("Amount", fontSize = 10.sp) }
                                                )
                                                AssistChip(
                                                    onClick = {
                                                        val hrs = snippet.filter { it.isDigit() || it == '.' }
                                                        parsedData = parsedData.copy(hours = hrs)
                                                    },
                                                    label = { Text("Hours", fontSize = 10.sp) }
                                                )
                                            }
                                            AssistChip(
                                                onClick = { parsedData = parsedData.copy(location = snippet) },
                                                label = { Text("Location", fontSize = 10.sp) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==================== EDITABLE PREVIEW OF EXTRACTED FIELDS ====================
                Text("Extracted Form Fields (Review or Edit below):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = parsedData.name,
                            onValueChange = { parsedData = parsedData.copy(name = it) },
                            label = { Text("Name / Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (targetType != "Vehicle") {
                            OutlinedTextField(
                                value = parsedData.mobileNumber,
                                onValueChange = { parsedData = parsedData.copy(mobileNumber = it) },
                                label = { Text("Mobile Number") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (targetType == "Vehicle" || targetType == "Work") {
                            OutlinedTextField(
                                value = parsedData.vehicleNumber,
                                onValueChange = { parsedData = parsedData.copy(vehicleNumber = it) },
                                label = { Text("Vehicle Number") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (targetType == "Driver") {
                            OutlinedTextField(
                                value = parsedData.aadhaarNumber,
                                onValueChange = { parsedData = parsedData.copy(aadhaarNumber = it) },
                                label = { Text("Aadhaar Number") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (targetType == "Customer") {
                            OutlinedTextField(
                                value = parsedData.gstin,
                                onValueChange = { parsedData = parsedData.copy(gstin = it) },
                                label = { Text("GSTIN (Optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (targetType == "Work") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = parsedData.hours,
                                    onValueChange = { parsedData = parsedData.copy(hours = it) },
                                    label = { Text("Hours") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = parsedData.rate,
                                    onValueChange = { parsedData = parsedData.copy(rate = it) },
                                    label = { Text("Rate (₹/hr)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            OutlinedTextField(
                                value = parsedData.amount,
                                onValueChange = { parsedData = parsedData.copy(amount = it) },
                                label = { Text("Amount Paid / Fixed (₹)") },
                                modifier = Modifier.fillMaxWidth()
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
                colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
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

/**
 * Parses raw text using standard Indian business patterns:
 * - 10-digit mobile numbers: [6-9]\d{9}
 * - Indian vehicle registration numbers: e.g. MH 09 AB 1234, KA 28 M 5678
 * - Aadhaar numbers: 12-digit grouped digits
 * - GSTIN: 15-character alphanumeric
 * - Hours, rates, and amounts
 */
fun parseDocumentText(text: String): ScannedRecordData {
    if (text.isBlank()) return ScannedRecordData()

    // 1. Mobile Number extraction (Indian 10-digit starting with 6-9)
    val phoneRegex = Regex("""\b[6-9]\d{9}\b""")
    val foundPhone = phoneRegex.find(text)?.value ?: ""

    // 2. Vehicle Number (Indian RTO pattern, e.g. MH 09 AB 1234, MH09-AB-1234)
    val vehicleRegex = Regex("""\b([A-Z]{2}[ -]?[0-9]{1,2}[ -]?[A-Z]{1,3}[ -]?[0-9]{4})\b""", RegexOption.IGNORE_CASE)
    val foundVehicle = vehicleRegex.find(text)?.value?.uppercase()?.replace("-", " ") ?: ""

    // 3. Aadhaar Number (12 digits, often with space/hyphen)
    val aadhaarRegex = Regex("""\b\d{4}[ -]?\d{4}[ -]?\d{4}\b""")
    val foundAadhaar = aadhaarRegex.find(text)?.value?.replace(" ", "")?.replace("-", "") ?: ""

    // 4. GSTIN (15 characters: 2 digits state code + 10 PAN + 1 entity + Z + 1 check digit)
    val gstinRegex = Regex("""\b\d{2}[A-Z]{5}\d{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}\b""", RegexOption.IGNORE_CASE)
    val foundGstin = gstinRegex.find(text)?.value?.uppercase() ?: ""

    // 5. Amount parsing (looks for ₹ or Rs. or Amount: followed by numbers)
    val amountRegex = Regex("""(?:(?:\b(?:Rs\.?|INR|Amount|Amt|Total|Paid)\b|₹)\s*[:=-]?\s*)(\d+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
    val foundAmount = amountRegex.find(text)?.groups?.get(1)?.value ?: ""

    // 6. Hours worked (looks for e.g. "25 hrs", "8 hours", "12.5 तास")
    val hoursRegex = Regex("""\b(\d+(?:\.\d{1,2})?)\s*(?:hrs?|hours?|तास)\b""", RegexOption.IGNORE_CASE)
    val foundHours = hoursRegex.find(text)?.groups?.get(1)?.value ?: ""

    // 7. Rate (looks for e.g. "@ 1200", "1500/hr", "दर 1000", "rate: 1200")
    val rateRegex = Regex("""(?:(?:\b(?:rate|दर)\b|@)\s*[:=-]?\s*(\d+))|(?:\b(\d+)\s*(?:\/hr|\/ तास|\/hour)\b)""", RegexOption.IGNORE_CASE)
    val rateMatch = rateRegex.find(text)
    val foundRate = rateMatch?.groups?.get(1)?.value ?: rateMatch?.groups?.get(2)?.value ?: ""

    // 8. Name heuristic: first line that doesn't start with numbers or labels
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    val foundName = lines.firstOrNull { line ->
        !line.startsWith("₹") && !line.startsWith("Rs") &&
                !line.contains("GST", ignoreCase = true) &&
                line.count { it.isLetter() } >= 3 &&
                line.length in 3..40
    } ?: ""

    // 9. Location / Site address detection
    val locationRegex = Regex("""(?:\b(?:Site\s+at|Work\s+at|Site|Location|ठिकाण|गाव|साइट|At)\b)\s*[:=-]?\s*([A-Za-z0-9 \t,\-]+)""", RegexOption.IGNORE_CASE)
    val foundLocation = locationRegex.find(text)?.groups?.get(1)?.value?.trim() ?: ""

    return ScannedRecordData(
        name = foundName,
        mobileNumber = foundPhone,
        vehicleNumber = foundVehicle,
        aadhaarNumber = foundAadhaar,
        gstin = gstinGstinCheck(foundGstin),
        amount = foundAmount,
        hours = foundHours,
        rate = foundRate,
        location = foundLocation,
        rawText = text
    )
}

private fun gstinGstinCheck(gstin: String): String {
    return if (gstin.length == 15) gstin else ""
}
