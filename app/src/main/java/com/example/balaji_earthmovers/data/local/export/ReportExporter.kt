package com.example.balaji_earthmovers.data.local.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.balaji_earthmovers.data.local.backup.BackupRestoreManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReportRowData(
    val code: String,
    val dateStr: String,
    val category: String,
    val title: String,
    val party: String,
    val vehicle: String,
    val driver: String,
    val hoursOrQty: Double,
    val rate: Double,
    val totalAmount: Double,
    val status: String,
    val paymentMode: String = "",
    val notes: String = "",
    val timestamp: Long = 0L
)

data class ReportKpiSummary(
    val totalRecords: Int,
    val totalBilled: Double,
    val totalExpenses: Double,
    val netMargin: Double,
    val totalHours: Double,
    val pendingCount: Int
)

object ReportExporter {

    private fun getReportsDirectory(context: Context): File {
        val baseDir = BackupRestoreManager.getBalajiStorageDirectory(context)
        val reportsDir = File(baseDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        return if (reportsDir.exists()) reportsDir else baseDir
    }

    /**
     * Generates a clean CSV file inside 'balajiearthmovers/reports/' folder.
     */
    fun exportToCsv(
        context: Context,
        rows: List<ReportRowData>,
        summary: ReportKpiSummary? = null,
        reportTitle: String = "OPERATIONAL REPORT"
    ): Result<File> {
        return try {
            val targetDir = getReportsDirectory(context)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(targetDir, "Balaji_${reportTitle.replace(" ", "_")}_$timeStamp.csv")

            file.bufferedWriter().use { writer ->
                // Summary block
                if (summary != null) {
                    writer.write("BALAJI EARTHMOVERS KODOLI - ${reportTitle.uppercase()}\n")
                    writer.write("Owner: Ajaysinh Jaysing Patil\n")
                    writer.write("Export Date: ${SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
                    writer.write("Total Records,Total Billed (INR),Total Expenses (INR),Net Operating Margin (INR),Total Hours\n")
                    writer.write("${summary.totalRecords},${"%.2f".format(summary.totalBilled)},${"%.2f".format(summary.totalExpenses)},${"%.2f".format(summary.netMargin)},${"%.1f".format(summary.totalHours)}\n\n")
                }

                // Table Header
                writer.write("ID,Date,Category,Title / Description,Party / Customer,Vehicle,Driver,Hours / Qty,Rate,Total Amount (INR),Status,Payment Mode,Notes\n")

                // Rows
                rows.forEach { row ->
                    val escapedTitle = escapeCsv(row.title)
                    val escapedParty = escapeCsv(row.party)
                    val escapedVehicle = escapeCsv(row.vehicle)
                    val escapedDriver = escapeCsv(row.driver)
                    val escapedNotes = escapeCsv(row.notes)

                    writer.write("${row.code},${row.dateStr},${row.category},$escapedTitle,$escapedParty,$escapedVehicle,$escapedDriver,${row.hoursOrQty},${row.rate},${"%.2f".format(row.totalAmount)},${row.status},${row.paymentMode},$escapedNotes\n")
                }
            }

            Result.success(file)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    /**
     * Generates a professional multi-page executive PDF report document using Android's native PdfDocument.
     * Saved into 'balajiearthmovers/reports/'.
     */
    fun exportToPdf(
        context: Context,
        rows: List<ReportRowData>,
        summary: ReportKpiSummary,
        reportTitle: String = "OPERATIONAL REPORT"
    ): Result<File> {
        val pdfDocument = PdfDocument()
        return try {
            val pageWidth = 595   // Standard A4 width in points
            val pageHeight = 842  // Standard A4 height in points
            val rowsPerPage = 22
            val totalPages = maxOf(1, (rows.size + rowsPerPage - 1) / rowsPerPage)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            var rowIndex = 0

            for (pageNumber in 1..totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                // 1. Top Decorative Brand Accent Strip (Gold)
                paint.color = AndroidColor.parseColor("#D97706")
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 8f, paint)

                var y = 34f

                // 2. Header Branding
                titlePaint.color = AndroidColor.parseColor("#0F172A")
                titlePaint.textSize = 15f
                titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("BALAJI EARTHMOVERS KODOLI", 24f, y, titlePaint)

                paint.color = AndroidColor.parseColor("#D97706")
                paint.textSize = 9.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(reportTitle.uppercase(), pageWidth - 220f, y, paint)

                y += 14f
                paint.color = AndroidColor.parseColor("#64748B")
                paint.textSize = 9f
                paint.typeface = Typeface.DEFAULT
                canvas.drawText("Owner: Ajaysinh Jaysing Patil | Kodoli, Kolhapur", 24f, y, paint)

                val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
                canvas.drawText("Generated: $dateStr", pageWidth - 170f, y, paint)

                y += 10f
                paint.color = AndroidColor.parseColor("#E2E8F0")
                canvas.drawLine(24f, y, pageWidth - 24f, y, paint)

                // 3. KPI Metrics Summary Bar (on Page 1)
                if (pageNumber == 1) {
                    y += 12f
                    // Draw KPI card background
                    paint.color = AndroidColor.parseColor("#F8FAFC")
                    canvas.drawRoundRect(24f, y, pageWidth - 24f, y + 46f, 6f, 6f, paint)
                    paint.color = AndroidColor.parseColor("#CBD5E1")
                    paint.style = Paint.Style.STROKE
                    canvas.drawRoundRect(24f, y, pageWidth - 24f, y + 46f, 6f, 6f, paint)
                    paint.style = Paint.Style.FILL

                    // KPI Columns inside the box
                    val kpiY = y + 18f
                    val kpiValY = y + 36f

                    // Metric 1: Total Records
                    paint.color = AndroidColor.parseColor("#64748B")
                    paint.textSize = 8f
                    canvas.drawText("RECORDS", 36f, kpiY, paint)
                    titlePaint.textSize = 11f
                    titlePaint.color = AndroidColor.parseColor("#0F172A")
                    canvas.drawText("${summary.totalRecords}", 36f, kpiValY, titlePaint)

                    // Metric 2: Billed
                    paint.color = AndroidColor.parseColor("#64748B")
                    canvas.drawText("TOTAL BILLED", 125f, kpiY, paint)
                    titlePaint.color = AndroidColor.parseColor("#059669")
                    canvas.drawText("₹${"%.0f".format(summary.totalBilled)}", 125f, kpiValY, titlePaint)

                    // Metric 3: Expense
                    paint.color = AndroidColor.parseColor("#64748B")
                    canvas.drawText("TOTAL EXPENSE", 235f, kpiY, paint)
                    titlePaint.color = AndroidColor.parseColor("#DC2626")
                    canvas.drawText("₹${"%.0f".format(summary.totalExpenses)}", 235f, kpiValY, titlePaint)

                    // Metric 4: Net Margin
                    paint.color = AndroidColor.parseColor("#64748B")
                    canvas.drawText("NET MARGIN", 355f, kpiY, paint)
                    titlePaint.color = if (summary.netMargin >= 0) AndroidColor.parseColor("#059669") else AndroidColor.parseColor("#DC2626")
                    canvas.drawText("₹${"%.0f".format(summary.netMargin)}", 355f, kpiValY, titlePaint)

                    // Metric 5: Total Hours
                    paint.color = AndroidColor.parseColor("#64748B")
                    canvas.drawText("WORK HOURS", 475f, kpiY, paint)
                    titlePaint.color = AndroidColor.parseColor("#D97706")
                    canvas.drawText("${"%.1f".format(summary.totalHours)}h", 475f, kpiValY, titlePaint)

                    y += 56f
                } else {
                    y += 16f
                }

                // 4. Data Table Header Row
                val tableHeaderHeight = 22f
                paint.color = AndroidColor.parseColor("#1E293B")
                canvas.drawRect(24f, y, pageWidth - 24f, y + tableHeaderHeight, paint)

                paint.color = AndroidColor.WHITE
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

                val colId = 28f
                val colDate = 76f
                val colCat = 136f
                val colTitle = 188f
                val colParty = 310f
                val colVehicle = 412f
                val colAmount = 492f
                val colStatus = 540f

                val headerTextY = y + 14f
                canvas.drawText("ID", colId, headerTextY, paint)
                canvas.drawText("Date", colDate, headerTextY, paint)
                canvas.drawText("Type", colCat, headerTextY, paint)
                canvas.drawText("Title / Work", colTitle, headerTextY, paint)
                canvas.drawText("Party / Customer", colParty, headerTextY, paint)
                canvas.drawText("Vehicle", colVehicle, headerTextY, paint)
                canvas.drawText("Amount", colAmount, headerTextY, paint)
                canvas.drawText("Status", colStatus, headerTextY, paint)

                y += tableHeaderHeight

                // 5. Data Rows for this Page
                val rowHeight = 22f
                paint.typeface = Typeface.DEFAULT
                paint.textSize = 8f

                val endRowIndex = minOf(rowIndex + rowsPerPage, rows.size)

                for (i in rowIndex until endRowIndex) {
                    val row = rows[i]
                    val isAlt = (i % 2 == 1)

                    // Zebra background
                    if (isAlt) {
                        paint.color = AndroidColor.parseColor("#F8FAFC")
                        canvas.drawRect(24f, y, pageWidth - 24f, y + rowHeight, paint)
                    }

                    // Divider line
                    paint.color = AndroidColor.parseColor("#F1F5F9")
                    canvas.drawLine(24f, y + rowHeight, pageWidth - 24f, y + rowHeight, paint)

                    val textY = y + 14f

                    // ID
                    paint.color = AndroidColor.parseColor("#0F172A")
                    canvas.drawText(truncate(row.code, 8), colId, textY, paint)

                    // Date
                    paint.color = AndroidColor.parseColor("#475569")
                    canvas.drawText(truncate(row.dateStr, 11), colDate, textY, paint)

                    // Category
                    paint.color = when (row.category.lowercase()) {
                        "work" -> AndroidColor.parseColor("#D97706")
                        "expense" -> AndroidColor.parseColor("#DC2626")
                        "income" -> AndroidColor.parseColor("#059669")
                        else -> AndroidColor.parseColor("#475569")
                    }
                    canvas.drawText(truncate(row.category, 8), colCat, textY, paint)

                    // Title
                    paint.color = AndroidColor.parseColor("#0F172A")
                    canvas.drawText(truncate(row.title, 22), colTitle, textY, paint)

                    // Party
                    paint.color = AndroidColor.parseColor("#334155")
                    canvas.drawText(truncate(row.party, 18), colParty, textY, paint)

                    // Vehicle
                    paint.color = AndroidColor.parseColor("#64748B")
                    canvas.drawText(truncate(row.vehicle, 14), colVehicle, textY, paint)

                    // Amount
                    paint.color = AndroidColor.parseColor("#0F172A")
                    canvas.drawText("₹${"%.0f".format(row.totalAmount)}", colAmount, textY, paint)

                    // Status
                    paint.color = when (row.status.lowercase()) {
                        "completed", "paid" -> AndroidColor.parseColor("#059669")
                        "running" -> AndroidColor.parseColor("#0284C7")
                        else -> AndroidColor.parseColor("#D97706")
                    }
                    canvas.drawText(truncate(row.status, 9), colStatus, textY, paint)

                    y += rowHeight
                }

                rowIndex = endRowIndex

                // 6. Footer (Page numbers and disclaimer)
                paint.color = AndroidColor.parseColor("#94A3B8")
                paint.textSize = 8f
                canvas.drawLine(24f, pageHeight - 32f, pageWidth - 24f, pageHeight - 32f, paint)
                canvas.drawText("Balaji Earthmovers ERP • Private & Confidential", 24f, pageHeight - 18f, paint)
                canvas.drawText("Page $pageNumber of $totalPages", pageWidth - 80f, pageHeight - 18f, paint)

                pdfDocument.finishPage(page)
            }

            // Save PDF file
            val targetDir = getReportsDirectory(context)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val pdfFile = File(targetDir, "Balaji_Report_$timeStamp.pdf")

            FileOutputStream(pdfFile).use { fos ->
                pdfDocument.writeTo(fos)
            }

            Result.success(pdfFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        } finally {
            pdfDocument.close()
        }
    }

    private fun truncate(str: String, maxLen: Int): String {
        return if (str.length > maxLen) str.take(maxLen - 2) + ".." else str
    }

    /**
     * Helper to open or share any generated File via Android's native share intent.
     */
    fun shareReportFile(context: Context, file: File, mimeType: String) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share Report via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
