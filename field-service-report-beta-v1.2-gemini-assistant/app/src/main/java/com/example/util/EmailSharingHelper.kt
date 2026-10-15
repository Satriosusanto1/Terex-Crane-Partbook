package com.example.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.FieldServiceReport
import java.io.File

/**
 * Predefined authorized WhatsApp operational groups for PT Amman site.
 */
enum class WhatsAppAuthorizedGroup(
    val id: String,
    val groupName: String,
    val subtitle: String,
    val description: String,
    val isLiebherr: Boolean
) {
    PT_AMMAN_NON_LIEBHERR(
        id = "amman_non_liebherr",
        groupName = "PT Amman Non Liebherr Operation",
        subtitle = "Operasional Unit Non-Liebherr",
        description = "Grup WhatsApp resmi untuk koordinasi & pelaporan armada alat berat Non-Liebherr di site PT Amman.",
        isLiebherr = false
    ),
    PT_AMMAN_LIEBHERR(
        id = "amman_liebherr",
        groupName = "PT Amman Liebherr Operation",
        subtitle = "Operasional Unit Liebherr",
        description = "Grup WhatsApp resmi untuk koordinasi & pelaporan armada alat berat Liebherr di site PT Amman.",
        isLiebherr = true
    );

    companion object {
        val ALL_GROUPS = listOf(PT_AMMAN_NON_LIEBHERR, PT_AMMAN_LIEBHERR)

        /**
         * Automatically suggest the most appropriate group based on unit brand/type.
         */
        fun getSuggestedGroup(report: FieldServiceReport): WhatsAppAuthorizedGroup {
            val brand = report.unitBrand.trim().lowercase()
            val model = report.type.trim().lowercase()
            return if (brand.contains("liebherr") || model.contains("liebherr") ||
                model.startsWith("r9") || model.startsWith("pr7") || model.startsWith("t2")
            ) {
                PT_AMMAN_LIEBHERR
            } else {
                PT_AMMAN_NON_LIEBHERR
            }
        }
    }
}

object EmailSharingHelper {

    val DEFAULT_TO_RECIPIENTS = listOf(
        "satrio.susanto@ptnim.co.id",
        "herry.mj@ptnim.co.id",
        "dias.wiluda@ptnim.co.id",
        "sudarmin@ptnim.co.id",
        "ade.septian@ptnim.co.id",
        "jiang.tao@ptnim.co.id"
    )

    val DEFAULT_CC_RECIPIENTS = listOf(
        "yudha.pratama@liebherr.com",
        "maryandi.maryandi@liebherr.com",
        "jacky.chew@liebherr.com",
        "cheewee.teo@liebherr.com"
    )

    /**
     * Shares the PDF report along with a structured WhatsApp summary caption directly to WhatsApp or WhatsApp Business,
     * targeted strictly to the specified authorized group (PT Amman Non Liebherr Operation / PT Amman Liebherr Operation).
     */
    fun shareReportByWhatsApp(
        context: Context,
        report: FieldServiceReport,
        pdfFile: File,
        targetGroup: WhatsAppAuthorizedGroup = WhatsAppAuthorizedGroup.getSuggestedGroup(report)
    ) {
        try {
            if (!pdfFile.exists()) {
                Toast.makeText(context, "File PDF tidak ditemukan untuk dibagikan", Toast.LENGTH_SHORT).show()
                return
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val unitInfo = if (report.unitNo.isNotBlank()) report.unitNo else "-"
            val modelInfo = "${report.unitBrand} ${report.type}".trim().ifBlank { "-" }
            val serialInfo = if (report.serialNo.isNotBlank()) report.serialNo else "-"
            val customerInfo = if (report.customerName.isNotBlank()) report.customerName else "-"
            val siteInfo = if (report.projectSite.isNotBlank()) report.projectSite else "-"
            val dateInfo = if (report.reportDate.isNotBlank()) report.reportDate else "-"
            val hmInfo = if (report.superstructureHourmeter.isNotBlank()) "${report.superstructureHourmeter} Hours" else "-"

            val waMessage = buildString {
                append("🎯 *TUJUAN GRUP:* \n")
                append("👥 *${targetGroup.groupName}*\n")
                append("═══════════════════════\n")
                append("📑 *FIELD SERVICE REPORT (FSR)*\n")
                append("═══════════════════════\n")
                append("🚜 *Unit No:* $unitInfo\n")
                append("⚙️ *Model:* $modelInfo\n")
                append("🔢 *Serial No:* $serialInfo\n")
                append("🏢 *Customer:* $customerInfo\n")
                append("📍 *Site:* $siteInfo\n")
                append("📅 *Tanggal:* $dateInfo\n")
                append("⏱️ *Hour Meter:* $hmInfo\n\n")

                if (report.jobDescription.isNotBlank()) {
                    append("📋 *Job Description:*\n")
                    append("${report.jobDescription}\n\n")
                }

                if (report.completedAction.isNotBlank()) {
                    append("🛠️ *Action Taken:*\n")
                    append("${report.completedAction}\n\n")
                }

                if (report.conclusion1.isNotBlank() || report.conclusion2.isNotBlank() || report.conclusion3.isNotBlank()) {
                    append("📌 *Kesimpulan & Rekomendasi:*\n")
                    if (report.conclusion1.isNotBlank()) append("1. ${report.conclusion1}\n")
                    if (report.conclusion2.isNotBlank()) append("2. ${report.conclusion2}\n")
                    if (report.conclusion3.isNotBlank()) append("3. ${report.conclusion3}\n")
                    append("\n")
                }

                append("👨‍🔧 *Prepared By:* ${report.preparedByName.ifBlank { "Service Engineer" }}")
                if (report.preparedByPosition.isNotBlank()) {
                    append(" (${report.preparedByPosition})")
                }
                append("\n═══════════════════════\n")
                append("📎 _Dokumen PDF resmi 2-halaman (${pdfFile.name}) terlampir._")
            }

            // Copy group name to clipboard for convenient paste/search in WhatsApp
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = ClipData.newPlainText("WhatsApp Target Group", targetGroup.groupName)
                clipboard?.setPrimaryClip(clip)
            } catch (_: Exception) {}

            Toast.makeText(
                context,
                "Membuka WhatsApp: Silakan pilih grup '${targetGroup.groupName}'",
                Toast.LENGTH_LONG
            ).show()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, waMessage)
                clipData = ClipData.newRawUri("Field Service Report PDF", contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Check if WhatsApp or WhatsApp Business is installed
            val pm = context.packageManager
            val waPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
            var targetPackage: String? = null

            for (pkg in waPackages) {
                try {
                    pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
                    targetPackage = pkg
                    break
                } catch (e: PackageManager.NameNotFoundException) {
                    // Continue checking
                }
            }

            if (targetPackage != null) {
                shareIntent.setPackage(targetPackage)
                context.grantUriPermission(targetPackage, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(shareIntent)
            } else {
                val chooser = Intent.createChooser(shareIntent, "Bagikan ke WhatsApp: ${targetGroup.groupName}").apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan ke WhatsApp: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Data class representing an installed email app on the device
     */
    data class InstalledEmailApp(
        val name: String,
        val packageName: String,
        val iconRes: Int? = null
    )

    /**
     * Returns the list of known email apps installed on this device.
     */
    fun getInstalledEmailApps(context: Context): List<InstalledEmailApp> {
        val pm = context.packageManager
        val knownApps = listOf(
            InstalledEmailApp("Gmail", "com.google.android.gm"),
            InstalledEmailApp("Microsoft Outlook", "com.microsoft.office.outlook"),
            InstalledEmailApp("Samsung Email", "com.samsung.android.email.provider"),
            InstalledEmailApp("Yahoo Mail", "com.yahoo.mobile.client.android.mail"),
            InstalledEmailApp("Thunderbird / K-9 Mail", "com.fsck.k9"),
            InstalledEmailApp("Spark Mail", "com.readdle.spark"),
            InstalledEmailApp("Aqua Mail", "org.kman.AquaMail"),
            InstalledEmailApp("Proton Mail", "ch.protonmail.android")
        )

        return knownApps.filter { app ->
            try {
                pm.getPackageInfo(app.packageName, PackageManager.GET_ACTIVITIES)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Triggers the user's default email app (Gmail, Outlook, Mail, etc.)
     * to send the generated PDF report as an attachment with prefilled recipients, subject, and body.
     */
    fun sendReportByEmail(context: Context, report: FieldServiceReport, pdfFile: File, specificPackage: String? = null) {
        try {
            if (!pdfFile.exists()) {
                Toast.makeText(context, "File PDF tidak ditemukan untuk dilampirkan", Toast.LENGTH_SHORT).show()
                return
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val unitInfo = if (report.unitNo.isNotBlank()) report.unitNo else "Unit"
            val customerInfo = if (report.customerName.isNotBlank()) report.customerName else "Customer"
            val dateInfo = if (report.reportDate.isNotBlank()) report.reportDate else "Laporan"
            val subject = "[FSR] Field Service Report - $unitInfo ($customerInfo) - $dateInfo"

            val body = buildString {
                append("Dear Team,\n\n")
                append("Berikut saya lampirkan dokumen resmi Field Service Report (FSR) hasil inspeksi dan servis unit lapangan:\n\n")
                append("📋 RINGKASAN UNIT & PEKERJAAN:\n")
                append("• No. Unit: ${report.unitNo.ifBlank { "-" }}\n")
                append("• Brand / Model: ${report.unitBrand} ${report.type}\n")
                append("• Serial Number: ${report.serialNo.ifBlank { "-" }}\n")
                append("• Project Site: ${report.projectSite.ifBlank { "-" }}\n")
                append("• Superstructure HM: ${report.superstructureHourmeter.ifBlank { "-" }}\n")
                append("• Chassis HM: ${report.chassisHourmeter.ifBlank { "-" }}\n")
                append("• Tanggal Laporan: ${report.reportDate.ifBlank { "-" }}\n\n")
                if (report.jobDescription.isNotBlank()) {
                    append("🔧 DESKRIPSI PEKERJAAN:\n")
                    append("${report.jobDescription}\n\n")
                }
                if (report.completedAction.isNotBlank()) {
                    append("📝 TINDAKAN PENANGANAN (COMPLETED ACTION):\n")
                    append("${report.completedAction}\n\n")
                }
                if (report.conclusion1.isNotBlank() || report.conclusion2.isNotBlank() || report.conclusion3.isNotBlank()) {
                    append("📌 KESIMPULAN & REKOMENDASI:\n")
                    if (report.conclusion1.isNotBlank()) append("1. ${report.conclusion1}\n")
                    if (report.conclusion2.isNotBlank()) append("2. ${report.conclusion2}\n")
                    if (report.conclusion3.isNotBlank()) append("3. ${report.conclusion3}\n")
                    append("\n")
                }
                append("Dokumen lengkap format PDF standar 2 halaman (${pdfFile.name}) yang memuat foto dokumentasi temuan dan tanda tangan digital telah dilampirkan pada email ini.\n\n")
                append("Hormat kami,\n")
                append("${report.preparedByName.ifBlank { "Field Service Team" }}")
                if (report.preparedByPosition.isNotBlank()) {
                    append("\n${report.preparedByPosition}")
                }
            }

            // Combine predefined TO recipients with any custom recipient typed in the field
            val extraRecipients = if (report.recipientEmail.isNotBlank()) {
                report.recipientEmail
                    .split(",", ";")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            } else {
                emptyList()
            }
            val toRecipients = (DEFAULT_TO_RECIPIENTS + extraRecipients).distinct().toTypedArray()
            val ccRecipients = DEFAULT_CC_RECIPIENTS.toTypedArray()

            // Construct standard ACTION_SEND intent with message/rfc822 for universal email client recognition
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, toRecipients)
                putExtra(Intent.EXTRA_CC, ccRecipients)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                putExtra(Intent.EXTRA_STREAM, contentUri)
                clipData = ClipData.newRawUri("Field Service Report PDF", contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Grant URI permission to all potential email clients and known packages
            val pm = context.packageManager
            val targetPackages = mutableSetOf(
                "com.google.android.gm",
                "com.microsoft.office.outlook",
                "com.samsung.android.email.provider",
                "com.yahoo.mobile.client.android.mail",
                "com.fsck.k9",
                "com.readdle.spark",
                "org.kman.AquaMail",
                "ch.protonmail.android",
                "com.my.mail",
                "com.zoho.mail"
            )

            try {
                val resolvedActivities = pm.queryIntentActivities(emailIntent, PackageManager.MATCH_DEFAULT_ONLY)
                for (resolveInfo in resolvedActivities) {
                    targetPackages.add(resolveInfo.activityInfo.packageName)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            for (pkg in targetPackages) {
                try {
                    context.grantUriPermission(pkg, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {
                    // Ignore non-existent package permissions
                }
            }

            // If a specific email app is requested (e.g. Gmail or Outlook directly)
            if (!specificPackage.isNullOrBlank()) {
                emailIntent.setPackage(specificPackage)
                context.startActivity(emailIntent)
                Toast.makeText(context, "Membuka aplikasi email...", Toast.LENGTH_SHORT).show()
                return
            }

            // Otherwise, show the system Email Chooser with a clear title
            val chooser = Intent.createChooser(emailIntent, "Kirim PDF Laporan via Aplikasi Email").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            // Fallback to generic application/pdf send
            try {
                val contentUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_SUBJECT, "[FSR] Field Service Report - ${report.unitNo}")
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    clipData = ClipData.newRawUri("Field Service Report PDF", contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(fallbackIntent, "Kirim Dokumen PDF...")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(context, "Gagal membuka aplikasi email: ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membuka aplikasi email: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * General Share Sheet to share the PDF across any compatible app (Drive, WhatsApp, etc.)
     */
    fun shareReportPdf(context: Context, pdfFile: File, report: FieldServiceReport) {
        try {
            if (!pdfFile.exists()) {
                Toast.makeText(context, "File PDF tidak ditemukan", Toast.LENGTH_SHORT).show()
                return
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val unitInfo = if (report.unitNo.isNotBlank()) report.unitNo else "Unit"
            val subject = "Field Service Report - $unitInfo"

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_STREAM, contentUri)
                clipData = ClipData.newRawUri("Field Service Report PDF", contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Bagikan Dokumen PDF FSR...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens the generated PDF in an external PDF viewer app (Google Drive PDF Viewer, Adobe Acrobat, etc.)
     */
    fun openPdfFile(context: Context, pdfFile: File) {
        try {
            if (!pdfFile.exists()) {
                Toast.makeText(context, "File PDF belum dibuat", Toast.LENGTH_SHORT).show()
                return
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                clipData = ClipData.newRawUri("Field Service Report PDF", contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Buka Laporan PDF dengan...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Tidak ada aplikasi pembaca PDF ditemukan", Toast.LENGTH_SHORT).show()
        }
    }
}
