package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.FieldServiceReport
import com.example.data.model.parsePartsJson
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    // Standard A4 dimensions in points (72 points per inch)
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun generateReportPdf(context: Context, report: FieldServiceReport): File {
        val pdfDocument = PdfDocument()

        val margin = 36f // 0.5 inch margin
        val contentWidth = PAGE_WIDTH - (margin * 2)

        // ================= PAGE 1 =================
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1 = page1.canvas
        drawPage1(context, canvas1, report, margin, contentWidth)
        pdfDocument.finishPage(page1)

        // ================= PAGE 2 =================
        val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        val canvas2 = page2.canvas
        drawPage2(context, canvas2, report, margin, contentWidth)
        pdfDocument.finishPage(page2)

        // Save to file
        val outputDir = File(context.filesDir, "generated_reports")
        if (!outputDir.exists()) outputDir.mkdirs()

        val fileName = getReportPdfFileName(report)
        val outputFile = File(outputDir, fileName)

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    /**
     * Formats the PDF filename according to user specification:
     * "<Unit No> - <Job Description> - <Inspection Date>.pdf", e.g. "MC041 - Area engine leak - 20-08-2026.pdf"
     */
    fun getReportPdfFileName(report: FieldServiceReport): String {
        val unit = report.unitNo.trim().ifBlank { "Unit" }
        val job = report.jobDescription.trim().ifBlank { "Field Service Report" }
        val rawDate = report.reportDate.trim().ifBlank {
            try {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(report.createdAt))
            } catch (_: Exception) {
                ""
            }
        }
        val safeDate = rawDate.replace("/", "-").replace(":", "-").trim()

        val safeName = if (safeDate.isNotBlank()) {
            "$unit - $job - $safeDate"
        } else {
            "$unit - $job"
        }.replace("[\\\\/:*?\"<>|\\r\\n]+".toRegex(), " ").trim()

        val truncatedName = if (safeName.length > 100) safeName.take(100).trim() else safeName
        return "$truncatedName.pdf"
    }

    private fun drawPage1(
        context: Context,
        canvas: Canvas,
        report: FieldServiceReport,
        margin: Float,
        contentWidth: Float
    ) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val headerBrandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 100, 100)
            textSize = 20f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
        }

        val orangeBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 158, 11) // Warm Orange accent
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }

        val dottedLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(160, 160, 160)
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(2.5f, 2.5f), 0f)
        }

        var currentY = 28f

        // 1. Brand Header
        val brandText = if (report.companyHeader.isNotBlank()) report.companyHeader else "LIEBHERR"
        canvas.drawText(brandText, PAGE_WIDTH / 2f, currentY + 16f, headerBrandPaint)
        currentY += 28f

        // 2. Orange Accent Line
        canvas.drawLine(margin, currentY, margin + contentWidth, currentY, orangeBarPaint)

        // Page Number Indicator (Top Right)
        val pageNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(120, 120, 120)
            textSize = 7.5f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("1 of 2", margin + contentWidth, currentY + 8f, pageNumPaint)
        currentY += 12f

        // 3. Document Title
        canvas.drawText(report.reportTitle.ifBlank { "FIELD SERVICE REPORT" }, PAGE_WIDTH / 2f, currentY + 12f, titlePaint)
        currentY += 22f

        // 4. Customer & Unit Information Grid (Dotted rows exactly as in image.png)
        val translatedJobDescription = if (report.jobDescription.isNotBlank()) {
            TechnicalTranslator.translateToEnglish(report.jobDescription)
        } else {
            report.jobDescription
        }

        val infoItems = listOf(
            "Customer Name" to report.customerName,
            "Project Site" to report.projectSite,
            "Contact Person" to report.contactPerson,
            "Unit No." to report.unitNo,
            "Unit Brand" to report.unitBrand,
            "Type" to report.type,
            "Serial No." to report.serialNo,
            "Superstructure hourmeter" to report.superstructureHourmeter,
            "Chassis hourmeter" to report.chassisHourmeter,
            "Odometer" to report.odometer,
            "Job Description" to translatedJobDescription,
            "Date of Inspection" to report.reportDate
        )

        val infoTableTop = currentY
        val rowHeight = 12f
        val colLabelWidth = 125f
        val colColonWidth = 12f

        // Draw bounding box for info table with dotted outline
        val infoTableHeight = infoItems.size * rowHeight
        canvas.drawRect(margin, infoTableTop, margin + contentWidth, infoTableTop + infoTableHeight, dottedLinePaint)

        infoItems.forEachIndexed { index, (label, value) ->
            val rowY = infoTableTop + (index * rowHeight)
            // Label
            canvas.drawText(label, margin + 4f, rowY + 9f, boldPaint)
            // Colon
            canvas.drawText(":", margin + colLabelWidth + 3f, rowY + 9f, boldPaint)
            // Value
            canvas.drawText(value, margin + colLabelWidth + colColonWidth + 4f, rowY + 9f, textPaint)

            // Dotted separator between rows
            if (index < infoItems.size - 1) {
                canvas.drawLine(margin, rowY + rowHeight, margin + contentWidth, rowY + rowHeight, dottedLinePaint)
            }
        }
        // Vertical dotted line separating labels
        canvas.drawLine(margin + colLabelWidth, infoTableTop, margin + colLabelWidth, infoTableTop + infoTableHeight, dottedLinePaint)
        canvas.drawLine(margin + colLabelWidth + colColonWidth, infoTableTop, margin + colLabelWidth + colColonWidth, infoTableTop + infoTableHeight, dottedLinePaint)

        currentY = infoTableTop + infoTableHeight + 10f

        // 5. Detail Pictures & Findings Table (Solid borders matching image.png)
        val photoTableTop = currentY
        val photoTableBottom = PAGE_HEIGHT - 32f
        val photoTableHeight = photoTableBottom - photoTableTop

        val colNoWidth = 24f
        val colPicWidth = 200f
        val colFindingsWidth = contentWidth - colNoWidth - colPicWidth

        // Table Header
        val headerHeight = 15f
        val tableHeaderBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Outer border
        canvas.drawRect(margin, photoTableTop, margin + contentWidth, photoTableBottom, linePaint)
        // Header bottom separator line
        canvas.drawLine(margin, photoTableTop + headerHeight, margin + contentWidth, photoTableTop + headerHeight, linePaint)

        // Header column texts
        canvas.drawText("NO.", margin + (colNoWidth / 2f), photoTableTop + 10.5f, tableHeaderBold)
        canvas.drawText("DETAIL PICTURES", margin + colNoWidth + (colPicWidth / 2f), photoTableTop + 10.5f, tableHeaderBold)
        canvas.drawText("FINDINGS AND RECOMMENDATIONS", margin + colNoWidth + colPicWidth + (colFindingsWidth / 2f), photoTableTop + 10.5f, tableHeaderBold)

        // Vertical column dividers
        canvas.drawLine(margin + colNoWidth, photoTableTop, margin + colNoWidth, photoTableBottom, linePaint)
        canvas.drawLine(margin + colNoWidth + colPicWidth, photoTableTop, margin + colNoWidth + colPicWidth, photoTableBottom, linePaint)

        // NO. "1." in column 1
        canvas.drawText("1.", margin + (colNoWidth / 2f), photoTableTop + headerHeight + 16f, boldPaint.apply { textAlign = Paint.Align.CENTER })
        boldPaint.textAlign = Paint.Align.LEFT

        // 3 photo rows inside DETAIL PICTURES column
        val photoRows = listOf(
            Triple("Foto Hourmeter", report.hourmeterPhotoUri, ""),
            Triple("Foto Defect 1", report.defect1PhotoUri, report.defect1Findings),
            Triple("Foto Defect 2", report.defect2PhotoUri, report.defect2Findings)
        )

        val totalPhotoAreaHeight = photoTableHeight - headerHeight
        val singlePhotoSlotHeight = totalPhotoAreaHeight / 3f

        // Aggregate findings text for the right column (automatically translated to English for international FSR standard)
        val findingsBuilder = StringBuilder()
        if (report.history.isNotBlank()) {
            val translatedHistory = OnlineTranslationManager.translateToEnglishSync(report.history)
            findingsBuilder.append("• History:\n").append(translatedHistory).append("\n\n")
        }
        if (report.defect1Findings.isNotBlank()) {
            val translatedDefect1 = OnlineTranslationManager.translateToEnglishSync(report.defect1Findings)
            findingsBuilder.append("• Defect 1:\n").append(translatedDefect1).append("\n\n")
        }
        if (report.defect2Findings.isNotBlank()) {
            val translatedDefect2 = OnlineTranslationManager.translateToEnglishSync(report.defect2Findings)
            findingsBuilder.append("• Defect 2:\n").append(translatedDefect2).append("\n\n")
        }
        if (findingsBuilder.isEmpty() && report.hourmeterFindings.isNotBlank()) {
            val translatedHourmeter = OnlineTranslationManager.translateToEnglishSync(report.hourmeterFindings)
            findingsBuilder.append("• Hourmeter:\n").append(translatedHourmeter).append("\n\n")
        }
        val aggregatedFindings = findingsBuilder.toString().trim()

        // Draw the 3 photo boxes
        photoRows.forEachIndexed { index, (defaultLabel, photoUri, findings) ->
            val slotTop = photoTableTop + headerHeight + (index * singlePhotoSlotHeight)
            val slotBottom = slotTop + singlePhotoSlotHeight

            // Box dimensions inside col 2 with margin
            val boxPaddingX = 8f
            val boxPaddingY = 8f
            val photoBoxRect = RectF(
                margin + colNoWidth + boxPaddingX,
                slotTop + boxPaddingY,
                margin + colNoWidth + colPicWidth - boxPaddingX,
                slotBottom - boxPaddingY
            )

            // Draw photo or placeholder box with label
            var photoBitmap: Bitmap? = null
            if (!photoUri.isNullOrBlank()) {
                photoBitmap = ImageUtils.loadScaledBitmap(context, photoUri, photoBoxRect.width().toInt(), photoBoxRect.height().toInt())
            }

            if (photoBitmap != null) {
                canvas.drawBitmapFitInside(photoBitmap, photoBoxRect, linePaint)
            } else {
                // Box outline matching image.png
                canvas.drawRect(photoBoxRect, linePaint)
                val placeholderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    textSize = 11f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(defaultLabel, photoBoxRect.centerX(), photoBoxRect.centerY() + 4f, placeholderPaint)
            }
        }

        // Draw Findings and Recommendations in the right column
        val findingsRect = RectF(
            margin + colNoWidth + colPicWidth + 8f,
            photoTableTop + headerHeight + 8f,
            margin + contentWidth - 8f,
            photoTableBottom - 8f
        )
        if (aggregatedFindings.isNotEmpty()) {
            drawWrappedText(canvas, aggregatedFindings, findingsRect, textPaint)
        }
    }

    private fun drawPage2(
        context: Context,
        canvas: Canvas,
        report: FieldServiceReport,
        margin: Float,
        contentWidth: Float
    ) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val headerBrandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 100, 100)
            textSize = 20f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
        }

        val orangeBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 158, 11) // Warm Orange accent
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }

        var currentY = 28f

        // 1. Brand Header
        val brandText = if (report.companyHeader.isNotBlank()) report.companyHeader else "LIEBHERR"
        canvas.drawText(brandText, PAGE_WIDTH / 2f, currentY + 16f, headerBrandPaint)
        currentY += 28f

        // 2. Orange Accent Line
        canvas.drawLine(margin, currentY, margin + contentWidth, currentY, orangeBarPaint)

        // Page Number Indicator (Top Right)
        val pageNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(120, 120, 120)
            textSize = 7.5f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("2 of 2", margin + contentWidth, currentY + 8f, pageNumPaint)
        currentY += 12f

        // 3. Top Split Section: "Part Required :" (Left) and "Completed Action:" (Right)
        val topSectionTop = currentY
        val splitWidth = contentWidth / 2f
        val topSectionHeight = 350f
        val topSectionBottom = topSectionTop + topSectionHeight

        // Draw bounding box for the entire top split
        canvas.drawRect(margin, topSectionTop, margin + contentWidth, topSectionBottom, linePaint)
        // Split vertical divider
        canvas.drawLine(margin + splitWidth, topSectionTop, margin + splitWidth, topSectionBottom, linePaint)

        // --- LEFT: Part Required : ---
        val sectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("Part Required :", margin + 6f, topSectionTop + 14f, sectionTitlePaint)

        // Underline "Required :" as in the template
        val partTitleWidth = sectionTitlePaint.measureText("Part ")
        val reqTitleWidth = sectionTitlePaint.measureText("Required :")
        val underlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 58, 138) // dark blue underline
            strokeWidth = 1.2f
        }
        canvas.drawLine(margin + 6f + partTitleWidth, topSectionTop + 16f, margin + 6f + partTitleWidth + reqTitleWidth, topSectionTop + 16f, underlinePaint)

        // Parts table inside left section
        val partsTableTop = topSectionTop + 22f
        val partsTableLeft = margin + 4f
        val partsTableRight = margin + splitWidth - 4f
        val partsTableBottom = topSectionBottom - 6f
        val partsTableWidth = partsTableRight - partsTableLeft
        val colPnWidth = 65f
        val colQtyWidth = 26f
        val colDescWidth = partsTableWidth - colPnWidth - colQtyWidth

        val numRows = 15
        val partRowHeight = (partsTableBottom - partsTableTop) / (numRows + 1) // 1 header + 15 rows

        // Parts table outline
        canvas.drawRect(partsTableLeft, partsTableTop, partsTableRight, partsTableBottom, linePaint)
        // Parts table header line
        canvas.drawLine(partsTableLeft, partsTableTop + partRowHeight, partsTableRight, partsTableTop + partRowHeight, linePaint)

        // Parts table vertical dividers
        canvas.drawLine(partsTableLeft + colPnWidth, partsTableTop, partsTableLeft + colPnWidth, partsTableBottom, linePaint)
        canvas.drawLine(partsTableLeft + colPnWidth + colDescWidth, partsTableTop, partsTableLeft + colPnWidth + colDescWidth, partsTableBottom, linePaint)

        // Headers
        val partHeaderBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Part Number", partsTableLeft + (colPnWidth / 2f), partsTableTop + 11f, partHeaderBold)
        canvas.drawText("Part Description", partsTableLeft + colPnWidth + (colDescWidth / 2f), partsTableTop + 11f, partHeaderBold)
        canvas.drawText("Qty", partsTableLeft + colPnWidth + colDescWidth + (colQtyWidth / 2f), partsTableTop + 11f, partHeaderBold)

        // Parse and fill parts
        val partsList = parsePartsJson(report.partsRequiredJson)
        val partCellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        for (i in 0 until numRows) {
            val rowY = partsTableTop + ((i + 1) * partRowHeight)
            val nextRowY = rowY + partRowHeight

            if (i < numRows - 1) {
                canvas.drawLine(partsTableLeft, nextRowY, partsTableRight, nextRowY, linePaint)
            }

            if (i < partsList.size) {
                val item = partsList[i]
                canvas.drawText(item.partNumber, partsTableLeft + 3f, rowY + 11f, partCellPaint)
                canvas.drawText(item.partDescription, partsTableLeft + colPnWidth + 3f, rowY + 11f, partCellPaint)
                canvas.drawText(item.qty, partsTableLeft + colPnWidth + colDescWidth + (colQtyWidth / 2f), rowY + 11f, partCellPaint.apply { textAlign = Paint.Align.CENTER })
                partCellPaint.textAlign = Paint.Align.LEFT
            }
        }

        // --- RIGHT: Completed Action: ---
        canvas.drawText("Completed Action:", margin + splitWidth + 6f, topSectionTop + 14f, sectionTitlePaint)

        // Completed action inner bordered box matching image.png
        val actionBoxLeft = margin + splitWidth + 6f
        val actionBoxRight = margin + contentWidth - 6f
        val actionBoxTop = topSectionTop + 22f
        val actionBoxBottom = topSectionBottom - 6f

        canvas.drawRect(actionBoxLeft, actionBoxTop, actionBoxRight, actionBoxBottom, linePaint)

        // Draw completed action text (translated to English)
        val translatedCompletedAction = if (report.completedAction.isNotBlank()) {
            OnlineTranslationManager.translateToEnglishSync(report.completedAction)
        } else {
            report.completedAction
        }
        val actionTextRect = RectF(actionBoxLeft + 6f, actionBoxTop + 6f, actionBoxRight - 6f, actionBoxBottom - 6f)
        drawWrappedText(canvas, translatedCompletedAction, actionTextRect, textPaint)

        currentY = topSectionBottom + 14f

        // 4. Middle Section: Conclusion
        val conclusionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isUnderlineText = true
        }
        canvas.drawText("Conclusion:", margin, currentY + 10f, conclusionTitlePaint)
        currentY += 16f

        // Conclusion 3 rows table
        val conclRowHeight = 17f
        val conclNumWidth = 20f
        val conclTableTop = currentY
        val conclTableBottom = conclTableTop + (3 * conclRowHeight)

        canvas.drawRect(margin, conclTableTop, margin + contentWidth, conclTableBottom, linePaint)
        canvas.drawLine(margin + conclNumWidth, conclTableTop, margin + conclNumWidth, conclTableBottom, linePaint)

        val rawConclusions = listOf(report.conclusion1, report.conclusion2, report.conclusion3)
        val conclusions = rawConclusions.map { if (it.isNotBlank()) OnlineTranslationManager.translateToEnglishSync(it) else it }
        for (i in 0 until 3) {
            val rowY = conclTableTop + (i * conclRowHeight)
            val nextRowY = rowY + conclRowHeight

            if (i < 2) {
                canvas.drawLine(margin, nextRowY, margin + contentWidth, nextRowY, linePaint)
            }

            // Number
            canvas.drawText("${i + 1}", margin + (conclNumWidth / 2f), rowY + 12f, boldPaint.apply { textAlign = Paint.Align.CENTER })
            boldPaint.textAlign = Paint.Align.LEFT

            // Text
            canvas.drawText(conclusions[i], margin + conclNumWidth + 5f, rowY + 12f, textPaint)
        }

        currentY = conclTableBottom + 16f

        // 5. Bottom Section: Signatures Box (3 Columns) - Standard FSR 3-box format
        val sigBoxHeight = 145f
        val sigBoxTop = currentY
        val sigBoxBottom = sigBoxTop + sigBoxHeight
        val sigColWidth = contentWidth / 3f

        // Outer box
        canvas.drawRect(margin, sigBoxTop, margin + contentWidth, sigBoxBottom, linePaint)
        // Vertical column dividers
        canvas.drawLine(margin + sigColWidth, sigBoxTop, margin + sigColWidth, sigBoxBottom, linePaint)
        canvas.drawLine(margin + (2 * sigColWidth), sigBoxTop, margin + (2 * sigColWidth), sigBoxBottom, linePaint)

        // Signature Info definition (Prepared By with digital signature, Acknowledge 2 & 3 with physical signature box)
        val cleanAckPosition = if (report.acknowledgeByPosition.contains("Supervisor Mechanic", ignoreCase = true) ||
            report.acknowledgeByPosition.contains("Technical Expert", ignoreCase = true)) {
            ""
        } else {
            report.acknowledgeByPosition
        }

        val cleanClientPosition = if (report.clientAcknowledgePosition.contains("Site Supervisor", ignoreCase = true)) {
            ""
        } else {
            report.clientAcknowledgePosition
        }

        val signatureColumns = listOf(
            SignatureColumnData(
                header = "Prepared By,",
                signatureUri = report.preparedBySignatureUri,
                name = report.preparedByName,
                position = report.preparedByPosition.ifBlank { "Mechanic / Inspector" }
            ),
            SignatureColumnData(
                header = "Acknowledge By,",
                signatureUri = null,
                name = report.acknowledgeByName,
                position = cleanAckPosition
            ),
            SignatureColumnData(
                header = report.clientAcknowledgeTitle.ifBlank { "Acknowledge By PT. AMNT" },
                signatureUri = null,
                name = report.clientAcknowledgeName,
                position = cleanClientPosition
            )
        )

        val labelRowHeight = 15f
        val nameRowTop = sigBoxBottom - (2 * labelRowHeight)
        val posRowTop = sigBoxBottom - labelRowHeight

        // Horizontal lines separating Name and Position rows
        canvas.drawLine(margin, nameRowTop, margin + contentWidth, nameRowTop, linePaint)
        canvas.drawLine(margin, posRowTop, margin + contentWidth, posRowTop, linePaint)

        val sigLabelColWidth = 44f

        signatureColumns.forEachIndexed { index, col ->
            val colLeft = margin + (index * sigColWidth)
            val colRight = colLeft + sigColWidth

            // Header title
            canvas.drawText(col.header, colLeft + 5f, sigBoxTop + 13f, boldPaint)

            // Signature Canvas / Stamp Area inside compact box
            if (!col.signatureUri.isNullOrBlank()) {
                val sigBitmap = ImageUtils.loadScaledBitmap(context, col.signatureUri, (sigColWidth - 16f).toInt(), (nameRowTop - sigBoxTop - 20f).toInt())
                if (sigBitmap != null) {
                    val sigRect = RectF(colLeft + 10f, sigBoxTop + 18f, colRight - 10f, nameRowTop - 5f)
                    canvas.drawBitmapFitInside(sigBitmap, sigRect, linePaint)
                }
            } else if (index > 0) {
                // Physical signature line for manual signing on PDF document
                val signLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.LTGRAY
                    strokeWidth = 0.6f
                    style = Paint.Style.STROKE
                }
                val signBaselineY = nameRowTop - 12f
                canvas.drawLine(colLeft + 14f, signBaselineY, colRight - 14f, signBaselineY, signLinePaint)
            }

            // Name Row: Label | Value
            canvas.drawLine(colLeft + sigLabelColWidth, nameRowTop, colLeft + sigLabelColWidth, sigBoxBottom, linePaint)
            canvas.drawText("Name", colLeft + 4f, nameRowTop + 11f, textPaint)
            canvas.drawText(col.name, colLeft + sigLabelColWidth + 4f, nameRowTop + 11f, boldPaint)

            // Position Row: Label | Value
            canvas.drawText("Position", colLeft + 4f, posRowTop + 11f, textPaint)
            canvas.drawText(col.position, colLeft + sigLabelColWidth + 4f, posRowTop + 11f, textPaint)
        }
    }

    private fun Canvas.drawBitmapFitInside(bitmap: Bitmap, destRect: RectF, borderPaint: Paint) {
        val srcWidth = bitmap.width.toFloat()
        val srcHeight = bitmap.height.toFloat()
        val destWidth = destRect.width()
        val destHeight = destRect.height()

        val scale = Math.min(destWidth / srcWidth, destHeight / srcHeight)
        val scaledWidth = srcWidth * scale
        val scaledHeight = srcHeight * scale

        val left = destRect.left + (destWidth - scaledWidth) / 2f
        val top = destRect.top + (destHeight - scaledHeight) / 2f
        val targetRect = RectF(left, top, left + scaledWidth, top + scaledHeight)

        val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
        drawBitmap(bitmap, srcRect, targetRect, null)
        drawRect(destRect, borderPaint)
    }

    private fun drawWrappedText(canvas: Canvas, text: String, bounds: RectF, paint: Paint) {
        if (text.isBlank()) return
        val words = text.split("\\s+".toRegex())
        var line = ""
        var y = bounds.top + paint.textSize + 2f
        val lineHeight = paint.textSize * 1.35f

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > bounds.width()) {
                if (y <= bounds.bottom - 2f) {
                    canvas.drawText(line, bounds.left, y, paint)
                    y += lineHeight
                }
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty() && y <= bounds.bottom) {
            canvas.drawText(line, bounds.left, y, paint)
        }
    }

    private data class SignatureColumnData(
        val header: String,
        val signatureUri: String?,
        val name: String,
        val position: String
    )
}
