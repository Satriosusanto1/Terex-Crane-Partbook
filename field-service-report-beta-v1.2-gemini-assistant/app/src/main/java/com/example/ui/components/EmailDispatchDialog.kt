package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FieldServiceReport
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.BentoTileBlue
import com.example.ui.theme.BentoTilePurple
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.IndustrialAmberDark
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.appTextFieldColors
import com.example.ui.theme.bento
import com.example.util.EmailSharingHelper
import java.io.File

@Composable
fun EmailDispatchDialog(
    report: FieldServiceReport,
    pdfFile: File,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val bento = MaterialTheme.bento
    val installedEmailApps = remember { EmailSharingHelper.getInstalledEmailApps(context) }

    val toCount = EmailSharingHelper.DEFAULT_TO_RECIPIENTS.size + (if (report.recipientEmail.isNotBlank()) 1 else 0)
    val ccCount = EmailSharingHelper.DEFAULT_CC_RECIPIENTS.size

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = bento.surface),
            border = BorderStroke(1.dp, bento.cardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = if (bento.isDark) 0.dp else 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
                .testTag("email_dispatch_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = bento.tilePurple,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = bento.primaryAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Kirim PDF via Email",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = bento.textPrimary
                            )
                            Text(
                                text = "Lampiran PDF & format pesan otomatis",
                                style = MaterialTheme.typography.bodySmall,
                                color = bento.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = bento.textSecondary)
                    }
                }

                HorizontalDivider(color = bento.cardBorderSubtle)

                // Info Dokumen & Penerima Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = bento.subtleSurface,
                    border = BorderStroke(1.dp, bento.cardBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // File attachment pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bento.surface, RoundedCornerShape(10.dp))
                                .border(1.dp, bento.cardBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = bento.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pdfFile.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = bento.textPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${(pdfFile.length() / 1024)} KB • Dokumen PDF Resmi 2 Halaman",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = bento.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Terlampir",
                                tint = SafetyGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Recipient counts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = bento.tilePurple,
                                border = BorderStroke(0.5.dp, bento.cardBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "TO: $toCount Penerima",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = bento.tilePurpleText,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = bento.tileAmber,
                                border = BorderStroke(0.5.dp, bento.cardBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "CC: $ccCount Tembusan",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = bento.tileAmberText,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // Available Email Applications Section
                Text(
                    text = "PILIH APLIKASI EMAIL DI HP:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = bento.textPrimary,
                    letterSpacing = 0.5.sp,
                    fontSize = 11.sp
                )

                // Main Direct Launch: Default Email Client
                Button(
                    onClick = {
                        EmailSharingHelper.sendReportByEmail(context, report, pdfFile)
                        onDismissRequest()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = bento.primaryAccent, contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("launch_default_email_app_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buka Aplikasi Email (Default)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                // Specific Apps quick shortcuts if installed
                if (installedEmailApps.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        installedEmailApps.forEach { app ->
                            OutlinedButton(
                                onClick = {
                                    EmailSharingHelper.sendReportByEmail(context, report, pdfFile, specificPackage = app.packageName)
                                    onDismissRequest()
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, bento.cardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("email_app_${app.name.lowercase().replace(" ", "_")}_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = bento.primaryAccent
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Buka via ${app.name}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = bento.textPrimary
                                )
                            }
                        }
                    }
                }

                // Copy Email Text helper
                OutlinedButton(
                    onClick = {
                        val unitInfo = if (report.unitNo.isNotBlank()) report.unitNo else "Unit"
                        val customerInfo = if (report.customerName.isNotBlank()) report.customerName else "Customer"
                        val dateInfo = if (report.reportDate.isNotBlank()) report.reportDate else "Laporan"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(
                            "FSR Email Body",
                            "Dear Team,\n\nBerikut saya lampirkan dokumen resmi Field Service Report (FSR) hasil inspeksi dan servis unit lapangan:\n\nSubject: [FSR] Field Service Report - $unitInfo ($customerInfo) - $dateInfo\n\nUnit: ${report.unitNo}\nBrand: ${report.unitBrand} ${report.type}\nSerial No: ${report.serialNo}\nSite: ${report.projectSite}\nJob: ${report.jobDescription}\nAction: ${report.completedAction}"
                        )
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Ringkasan email berhasil disalin ke clipboard", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, bento.cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = bento.textSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Salin Teks Ringkasan Email",
                        fontSize = 12.sp,
                        color = bento.textSecondary
                    )
                }
            }
        }
    }
}
